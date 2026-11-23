import crypto from 'crypto';
import jwt from 'jsonwebtoken';
import { OAuth2Client } from 'google-auth-library';
import { getApps, initializeApp, cert, applicationDefault } from 'firebase-admin/app';
import type { App } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';
import type { UserDto } from './types';

const JWT_ISSUER = 'court-finder-api';
const JWT_AUDIENCE = 'court-finder-android';

export function getJwtSecret(): string {
  const secret = process.env.COURTFINDER_JWT_SECRET;
  if (secret && secret.length >= 32) return secret;
  if (process.env.COURTFINDER_ENV === 'development' || process.env.NODE_ENV === 'development') {
    return 'local-development-secret-change-this-before-deploying';
  }
  return secret || 'default-secret-at-least-32-chars-long-courtfinder';
}

export function hashPassword(password: string, salt: Buffer): Buffer {
  return crypto.pbkdf2Sync(password, salt, 210000, 32, 'sha256');
}

export function generateSalt(): Buffer {
  return crypto.randomBytes(16);
}

export function verifyPassword(password: string, salt: Buffer, storedHash: Buffer): boolean {
  const computedHash = hashPassword(password, salt);
  if (computedHash.length !== storedHash.length) return false;
  return crypto.timingSafeEqual(computedHash, storedHash);
}

export function createAuthToken(user: UserDto): string {
  const secret = getJwtSecret();
  return jwt.sign(
    { email: user.email },
    secret,
    {
      issuer: JWT_ISSUER,
      audience: JWT_AUDIENCE,
      subject: user.id,
      expiresIn: '24h',
      algorithm: 'HS256',
    }
  );
}

export function verifyAuthToken(headerValue?: string | null): string {
  if (!headerValue || !headerValue.startsWith('Bearer ')) {
    throw new Error('Sign in to continue.');
  }
  const token = headerValue.substring(7).trim();
  const secret = getJwtSecret();
  try {
    const decoded = jwt.verify(token, secret, {
      issuer: JWT_ISSUER,
      audience: JWT_AUDIENCE,
    }) as jwt.JwtPayload;

    if (!decoded.sub) {
      throw new Error('Invalid token payload.');
    }
    return decoded.sub;
  } catch {
    throw new Error('Sign in to continue.');
  }
}

// Firebase Admin initialization
let firebaseApp: App | null = null;
function getFirebaseApp(): App | null {
  if (firebaseApp) return firebaseApp;
  const projectId = process.env.COURTFINDER_FIREBASE_PROJECT_ID || process.env.GOOGLE_CLOUD_PROJECT;
  if (!projectId) return null;

  try {
    const existingApps = getApps();
    if (existingApps.length > 0) {
      firebaseApp = existingApps[0];
      return firebaseApp;
    }

    const serviceAccountJson = process.env.COURTFINDER_FIREBASE_SERVICE_ACCOUNT_JSON;
    if (serviceAccountJson) {
      const credentials = JSON.parse(serviceAccountJson);
      firebaseApp = initializeApp({
        credential: cert(credentials),
        projectId,
      });
    } else {
      firebaseApp = initializeApp({
        credential: applicationDefault(),
        projectId,
      });
    }
    return firebaseApp;
  } catch (err) {
    console.error('Failed to initialize Firebase Admin:', err);
    return null;
  }
}

export interface VerifiedGoogleUser {
  subject: string;
  email: string;
  name?: string | null;
  firebaseUid?: string | null;
}

export async function verifyGoogleToken(idToken: string): Promise<VerifiedGoogleUser> {
  const decoded = jwt.decode(idToken, { complete: true });
  if (!decoded || typeof decoded.payload === 'string') {
    throw new Error('Google sign-in token is invalid or expired.');
  }

  const payload = decoded.payload as jwt.JwtPayload;
  const audience = Array.isArray(payload.aud) ? payload.aud : [payload.aud || ''];
  const firebaseProjectId = process.env.COURTFINDER_FIREBASE_PROJECT_ID || process.env.GOOGLE_CLOUD_PROJECT;

  // 1. If audience matches Firebase project, verify with Firebase Admin
  if (firebaseProjectId && audience.includes(firebaseProjectId)) {
    const app = getFirebaseApp();
    if (!app) {
      throw new Error('Firebase Google sign-in verification is unavailable. Configure Firebase Admin credentials.');
    }
    try {
      const decodedFb = await getAuth(app).verifyIdToken(idToken, true);
      const firebaseClaims = decodedFb.firebase;
      if (firebaseClaims.sign_in_provider !== 'google.com' || !decodedFb.email_verified || !decodedFb.email) {
        throw new Error('Firebase token is not a verified Google account.');
      }
      const googleIdentities = (firebaseClaims.identities as Record<string, string[]>)?.['google.com'];
      const googleSubject = googleIdentities?.[0];
      if (!googleSubject) {
        throw new Error('Google subject not found in Firebase token.');
      }
      return {
        subject: googleSubject,
        email: decodedFb.email,
        name: decodedFb.name || null,
        firebaseUid: decodedFb.uid,
      };
    } catch {
      throw new Error('Firebase Google sign-in token is invalid, expired, or not linked to a verified Google account.');
    }
  }

  // 2. Otherwise verify with standard Google OAuth2Client
  const clientId = process.env.COURTFINDER_GOOGLE_WEB_CLIENT_ID;
  if (!clientId) {
    throw new Error('Google sign-in is not configured. Set COURTFINDER_GOOGLE_WEB_CLIENT_ID.');
  }

  const client = new OAuth2Client(clientId);
  try {
    const ticket = await client.verifyIdToken({
      idToken,
      audience: clientId,
    });
    const ticketPayload = ticket.getPayload();
    if (!ticketPayload || !ticketPayload.email || !ticketPayload.email_verified) {
      throw new Error('Google account must have a verified email address.');
    }
    return {
      subject: ticketPayload.sub,
      email: ticketPayload.email,
      name: ticketPayload.name || null,
    };
  } catch {
    throw new Error('Google sign-in token is invalid or expired.');
  }
}
