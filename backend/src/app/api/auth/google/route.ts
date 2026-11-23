import { NextRequest } from 'next/server';
import { verifyGoogleToken, createAuthToken } from '@/lib/auth';
import { loginWithGoogleAccount, loginWithFirebaseGoogleAccount } from '@/lib/db';
import { successJson, errorJson, handleOptions } from '@/lib/api-response';

export async function POST(req: NextRequest) {
  try {
    const body = await req.json().catch(() => null);
    if (!body || !body.idToken) {
      return errorJson('Google sign-in token is invalid or expired.', 400);
    }

    const verified = await verifyGoogleToken(body.idToken);
    let user;
    if (verified.firebaseUid) {
      user = await loginWithFirebaseGoogleAccount(
        verified.firebaseUid,
        verified.subject,
        verified.email,
        verified.name
      );
    } else {
      user = await loginWithGoogleAccount(verified.subject, verified.email, verified.name);
    }

    const token = createAuthToken(user);
    return successJson({
      success: true,
      token,
      user,
    });
  } catch (err: any) {
    return errorJson(err.message || 'Google sign-in token is invalid or expired.', err.status || 401);
  }
}

export async function OPTIONS() {
  return handleOptions();
}
