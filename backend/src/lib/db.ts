import { Pool } from 'pg';
import crypto from 'crypto';
import { UserDto, CourtDto, BookingConfirmationDto } from './types';
import { hashPassword, generateSalt, verifyPassword } from './auth';

let pool: Pool | null = null;
let initialized = false;

export const DEMO_COURTS: CourtDto[] = [
  {
    id: 'c1',
    name: 'Apex Tennis Arena & Academy',
    sportType: 'Tennis',
    location: 'Downtown Sports Hub, Sector 4',
    distanceKm: 1.8,
    latitude: 37.7749,
    longitude: -122.4194,
    pricePerHour: '$25/hr',
    rating: 4.9,
    availableTimeSlots: ['06:00 PM', '07:00 PM', '09:00 PM'],
    aiMatchScore: 98,
    isFavorite: false,
    hasAlertSet: false,
    amenities: ['LED Floodlights', 'Free Parking', 'Changing Rooms', 'Racket Rental'],
  },
  {
    id: 'c2',
    name: 'Smash Badminton Indoor Club',
    sportType: 'Badminton',
    location: 'Eastside Fitness Park',
    distanceKm: 3.2,
    latitude: 37.7849,
    longitude: -122.4094,
    pricePerHour: '$18/hr',
    rating: 4.7,
    availableTimeSlots: ['05:30 PM', '08:00 PM'],
    aiMatchScore: 94,
    isFavorite: false,
    hasAlertSet: false,
    amenities: ['Air Conditioned', 'Shoe Rental', 'Hydration Station'],
  },
  {
    id: 'c3',
    name: 'Urban Hoops Basketball Arena',
    sportType: 'Basketball',
    location: 'Central Stadium Complex',
    distanceKm: 4.5,
    latitude: 37.7649,
    longitude: -122.4294,
    pricePerHour: '$30/hr',
    rating: 4.8,
    availableTimeSlots: ['07:00 PM', '08:30 PM'],
    aiMatchScore: 91,
    isFavorite: false,
    hasAlertSet: false,
    amenities: ['Sound System', 'Night Lights', 'Lockers & Showers'],
  },
  {
    id: 'c4',
    name: 'Pro Padel Court & Lounge',
    sportType: 'Padel',
    location: 'Green Valley Sports Club',
    distanceKm: 5.0,
    latitude: 37.7549,
    longitude: -122.4394,
    pricePerHour: '$35/hr',
    rating: 4.9,
    availableTimeSlots: ['06:00 PM', '07:30 PM'],
    aiMatchScore: 89,
    isFavorite: false,
    hasAlertSet: false,
    amenities: ['Cafe Lounge', 'Glass Panoramic Courts', 'Valet Parking'],
  },
  {
    id: 'c5',
    name: 'Rally Pickleball Center',
    sportType: 'Pickleball',
    location: 'Westside Community Sports Park',
    distanceKm: 2.6,
    latitude: 37.7949,
    longitude: -122.3994,
    pricePerHour: '$22/hr',
    rating: 4.6,
    availableTimeSlots: ['05:00 PM', '06:30 PM', '08:00 PM'],
    aiMatchScore: 92,
    isFavorite: false,
    hasAlertSet: false,
    amenities: ['Court Lighting', 'Free Parking', 'Water Station'],
  },
];

export function getDbPool(): Pool {
  if (!pool) {
    const connectionString = process.env.DATABASE_URL;
    if (!connectionString) {
      throw new Error('DATABASE_URL environment variable is required to connect to PostgreSQL.');
    }
    const isLocalhost = connectionString.includes('localhost') || connectionString.includes('127.0.0.1');
    pool = new Pool({
      connectionString,
      ssl: isLocalhost ? false : { rejectUnauthorized: false },
      max: 10,
      idleTimeoutMillis: 30000,
    });
  }
  return pool;
}

export async function ensureDatabase(): Promise<void> {
  if (initialized) return;
  const db = getDbPool();

  await db.query(`
    CREATE TABLE IF NOT EXISTS users (
      id TEXT PRIMARY KEY,
      full_name TEXT NOT NULL,
      email TEXT NOT NULL UNIQUE,
      phone TEXT NOT NULL DEFAULT '',
      password_salt BYTEA NOT NULL,
      password_hash BYTEA NOT NULL
    );

    CREATE TABLE IF NOT EXISTS courts (
      id TEXT PRIMARY KEY,
      name TEXT NOT NULL,
      sport_type TEXT NOT NULL,
      location TEXT NOT NULL,
      distance_km REAL NOT NULL,
      latitude REAL NOT NULL,
      longitude REAL NOT NULL,
      price_per_hour TEXT NOT NULL,
      rating REAL NOT NULL,
      time_slots TEXT NOT NULL,
      ai_match_score INTEGER NOT NULL,
      amenities TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS bookings (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL REFERENCES users(id),
      court_id TEXT NOT NULL REFERENCES courts(id),
      time_slot TEXT NOT NULL,
      booking_date TEXT NOT NULL,
      UNIQUE(court_id, time_slot, booking_date)
    );

    CREATE TABLE IF NOT EXISTS google_accounts (
      subject TEXT PRIMARY KEY,
      user_id TEXT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS firebase_accounts (
      firebase_uid TEXT PRIMARY KEY,
      user_id TEXT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE
    );
  `);

  // Seed courts if table is empty
  const countRes = await db.query('SELECT COUNT(*) FROM courts');
  if (parseInt(countRes.rows[0].count, 10) === 0) {
    for (const c of DEMO_COURTS) {
      await db.query(
        `INSERT INTO courts (id, name, sport_type, location, distance_km, latitude, longitude, price_per_hour, rating, time_slots, ai_match_score, amenities)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
         ON CONFLICT (id) DO NOTHING`,
        [
          c.id,
          c.name,
          c.sportType,
          c.location,
          c.distanceKm,
          c.latitude,
          c.longitude,
          c.pricePerHour,
          c.rating,
          JSON.stringify(c.availableTimeSlots),
          c.aiMatchScore,
          JSON.stringify(c.amenities),
        ]
      );
    }
  }

  initialized = true;
}

export async function registerUser(fullName: string, email: string, phone: string, password: string): Promise<UserDto> {
  const name = fullName.trim();
  const normalizedEmail = email.trim().toLowerCase();
  const cleanPhone = phone.trim();

  if (!name) throw { status: 400, message: 'Full name is required.' };
  if (!normalizedEmail.includes('@')) throw { status: 400, message: 'Enter a valid email address.' };
  if (password.length < 10) throw { status: 400, message: 'Password must be at least 10 characters.' };

  await ensureDatabase();
  const db = getDbPool();

  const salt = generateSalt();
  const passwordHash = hashPassword(password, salt);
  const id = crypto.randomUUID();

  try {
    await db.query(
      `INSERT INTO users (id, full_name, email, phone, password_salt, password_hash)
       VALUES ($1, $2, $3, $4, $5, $6)`,
      [id, name, normalizedEmail, cleanPhone, salt, passwordHash]
    );
  } catch (err: any) {
    if (err.code === '23505') {
      throw { status: 409, message: 'An account with that email already exists.' };
    }
    throw err;
  }

  return { id, name, email: normalizedEmail };
}

export async function loginUser(emailOrPhone: string, password: string): Promise<UserDto> {
  const identifier = emailOrPhone.trim().toLowerCase();
  const rawIdentifier = emailOrPhone.trim();

  await ensureDatabase();
  const db = getDbPool();

  const res = await db.query(
    `SELECT id, full_name, email, password_salt, password_hash
     FROM users
     WHERE LOWER(email) = $1 OR phone = $2
     LIMIT 1`,
    [identifier, rawIdentifier]
  );

  const row = res.rows[0];
  if (!row) {
    throw { status: 401, message: 'Email/phone or password is incorrect.' };
  }

  const salt = Buffer.from(row.password_salt);
  const storedHash = Buffer.from(row.password_hash);

  if (!verifyPassword(password, salt, storedHash)) {
    throw { status: 401, message: 'Email/phone or password is incorrect.' };
  }

  return {
    id: row.id,
    name: row.full_name,
    email: row.email,
  };
}

export async function getUser(userId: string): Promise<UserDto> {
  await ensureDatabase();
  const db = getDbPool();

  const res = await db.query(`SELECT id, full_name, email FROM users WHERE id = $1`, [userId]);
  const row = res.rows[0];
  if (!row) {
    throw { status: 401, message: 'Account is unavailable.' };
  }
  return { id: row.id, name: row.full_name, email: row.email };
}

export async function loginWithGoogleAccount(subject: string, email: string, name?: string | null): Promise<UserDto> {
  const normalizedEmail = email.trim().toLowerCase();
  if (!subject.trim() || !normalizedEmail.includes('@')) {
    throw { status: 401, message: 'Google account details are invalid.' };
  }

  await ensureDatabase();
  const db = getDbPool();

  // Check if linked
  const linkedRes = await db.query(
    `SELECT users.id, users.full_name, users.email
     FROM users
     JOIN google_accounts ON users.id = google_accounts.user_id
     WHERE google_accounts.subject = $1`,
    [subject]
  );
  if (linkedRes.rows.length > 0) {
    const row = linkedRes.rows[0];
    return { id: row.id, name: row.full_name, email: row.email };
  }

  // New Google account creation
  const fullName = name?.trim() || normalizedEmail.split('@')[0];
  const userId = crypto.randomUUID();
  const salt = generateSalt();
  const randomPassword = crypto.randomBytes(48).toString('base64');
  const passwordHash = hashPassword(randomPassword, salt);

  const client = await db.connect();
  try {
    await client.query('BEGIN');
    await client.query(
      `INSERT INTO users (id, full_name, email, phone, password_salt, password_hash)
       VALUES ($1, $2, $3, '', $4, $5)`,
      [userId, fullName, normalizedEmail, salt, passwordHash]
    );
    await client.query(
      `INSERT INTO google_accounts (subject, user_id) VALUES ($1, $2)`,
      [subject, userId]
    );
    await client.query('COMMIT');
    return { id: userId, name: fullName, email: normalizedEmail };
  } catch (err: any) {
    await client.query('ROLLBACK');
    if (err.code === '23505') {
      throw { status: 409, message: 'An account with this email already exists. Sign in with that account first.' };
    }
    throw err;
  } finally {
    client.release();
  }
}

export async function loginWithFirebaseGoogleAccount(
  firebaseUid: string,
  googleSubject: string,
  email: string,
  name?: string | null
): Promise<UserDto> {
  const normalizedEmail = email.trim().toLowerCase();
  if (!firebaseUid.trim() || !googleSubject.trim() || !normalizedEmail.includes('@')) {
    throw { status: 401, message: 'Firebase Google account details are invalid.' };
  }

  await ensureDatabase();
  const db = getDbPool();

  // 1. Check existing firebase_accounts
  const fbRes = await db.query(
    `SELECT users.id, users.full_name, users.email
     FROM users
     JOIN firebase_accounts ON users.id = firebase_accounts.user_id
     WHERE firebase_accounts.firebase_uid = $1`,
    [firebaseUid]
  );
  if (fbRes.rows.length > 0) {
    const row = fbRes.rows[0];
    return { id: row.id, name: row.full_name, email: row.email };
  }

  // 2. Check existing google_accounts
  const googleRes = await db.query(
    `SELECT users.id, users.full_name, users.email
     FROM users
     JOIN google_accounts ON users.id = google_accounts.user_id
     WHERE google_accounts.subject = $1`,
    [googleSubject]
  );

  const client = await db.connect();
  try {
    await client.query('BEGIN');

    let user: UserDto;
    if (googleRes.rows.length > 0) {
      const row = googleRes.rows[0];
      user = { id: row.id, name: row.full_name, email: row.email };
    } else {
      const fullName = name?.trim() || normalizedEmail.split('@')[0];
      const userId = crypto.randomUUID();
      const salt = generateSalt();
      const randomPassword = crypto.randomBytes(48).toString('base64');
      const passwordHash = hashPassword(randomPassword, salt);

      await client.query(
        `INSERT INTO users (id, full_name, email, phone, password_salt, password_hash)
         VALUES ($1, $2, $3, '', $4, $5)`,
        [userId, fullName, normalizedEmail, salt, passwordHash]
      );
      await client.query(
        `INSERT INTO google_accounts (subject, user_id) VALUES ($1, $2)`,
        [googleSubject, userId]
      );
      user = { id: userId, name: fullName, email: normalizedEmail };
    }

    await client.query(
      `INSERT INTO firebase_accounts (firebase_uid, user_id) VALUES ($1, $2)`,
      [firebaseUid, user.id]
    );

    await client.query('COMMIT');
    return user;
  } catch (err: any) {
    await client.query('ROLLBACK');
    if (err.code === '23505') {
      throw { status: 409, message: 'This Firebase account is already linked to another CourtFinder account.' };
    }
    throw err;
  } finally {
    client.release();
  }
}

export async function searchCourts(query: string = ''): Promise<CourtDto[]> {
  try {
    await ensureDatabase();
    const db = getDbPool();
    const res = await db.query(`SELECT * FROM courts ORDER BY rating DESC`);
    const allCourts: CourtDto[] = res.rows.map((row) => ({
      id: row.id,
      name: row.name,
      sportType: row.sport_type,
      location: row.location,
      distanceKm: parseFloat(row.distance_km),
      latitude: parseFloat(row.latitude),
      longitude: parseFloat(row.longitude),
      pricePerHour: row.price_per_hour,
      rating: parseFloat(row.rating),
      availableTimeSlots: typeof row.time_slots === 'string' ? JSON.parse(row.time_slots) : row.time_slots,
      aiMatchScore: parseInt(row.ai_match_score, 10),
      amenities: typeof row.amenities === 'string' ? JSON.parse(row.amenities) : row.amenities,
    }));

    const sports = ['Tennis', 'Badminton', 'Basketball', 'Padel', 'Pickleball'];
    const matchedSport = sports.find((s) => query.toLowerCase().includes(s.toLowerCase()));
    if (!matchedSport) return allCourts;
    return allCourts.filter((c) => c.sportType.toLowerCase() === matchedSport.toLowerCase());
  } catch (err) {
    // If DB is not configured yet, fallback gracefully to demo courts for search
    console.warn('DB not reachable, returning DEMO_COURTS:', err);
    const sports = ['Tennis', 'Badminton', 'Basketball', 'Padel', 'Pickleball'];
    const matchedSport = sports.find((s) => query.toLowerCase().includes(s.toLowerCase()));
    if (!matchedSport) return DEMO_COURTS;
    return DEMO_COURTS.filter((c) => c.sportType.toLowerCase() === matchedSport.toLowerCase());
  }
}

export async function bookCourt(userId: string, courtId: string, timeSlot: string): Promise<BookingConfirmationDto> {
  await ensureDatabase();
  const db = getDbPool();

  const courtRes = await db.query(`SELECT name, time_slots FROM courts WHERE id = $1`, [courtId]);
  const courtRow = courtRes.rows[0];
  if (!courtRow) {
    throw { status: 404, message: 'Court was not found.' };
  }

  const availableSlots: string[] = typeof courtRow.time_slots === 'string' ? JSON.parse(courtRow.time_slots) : courtRow.time_slots;
  if (!availableSlots.includes(timeSlot)) {
    throw { status: 400, message: 'That time slot is not available for this court.' };
  }

  const date = new Date().toISOString().split('T')[0];
  const bookingId = `BK-${crypto.randomUUID().slice(0, 8).toUpperCase()}`;

  try {
    await db.query(
      `INSERT INTO bookings (id, user_id, court_id, time_slot, booking_date)
       VALUES ($1, $2, $3, $4, $5)`,
      [bookingId, userId, courtId, timeSlot, date]
    );
  } catch (err: any) {
    if (err.code === '23505') {
      throw { status: 409, message: 'That time slot has already been booked.' };
    }
    throw err;
  }

  return {
    bookingId,
    courtName: courtRow.name,
    timeSlot,
    date,
  };
}

export async function deleteAccount(userId: string): Promise<void> {
  await ensureDatabase();
  const db = getDbPool();

  const client = await db.connect();
  try {
    await client.query('BEGIN');
    await client.query('DELETE FROM bookings WHERE user_id = $1', [userId]);
    await client.query('DELETE FROM google_accounts WHERE user_id = $1', [userId]);
    await client.query('DELETE FROM firebase_accounts WHERE user_id = $1', [userId]);
    const deleteRes = await client.query('DELETE FROM users WHERE id = $1', [userId]);
    if (deleteRes.rowCount === 0) {
      throw { status: 404, message: 'Account was not found.' };
    }
    await client.query('COMMIT');
  } catch (err) {
    await client.query('ROLLBACK');
    throw err;
  } finally {
    client.release();
  }
}
