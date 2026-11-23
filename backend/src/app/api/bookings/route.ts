import { NextRequest } from 'next/server';
import { verifyAuthToken } from '@/lib/auth';
import { bookCourt } from '@/lib/db';
import { successJson, errorJson, handleOptions } from '@/lib/api-response';

export async function POST(req: NextRequest) {
  try {
    const authHeader = req.headers.get('authorization');
    const userId = verifyAuthToken(authHeader);

    const body = await req.json().catch(() => null);
    if (!body || !body.courtId || !body.timeSlot) {
      return errorJson('Request body is invalid.', 400);
    }

    const booking = await bookCourt(userId, body.courtId, body.timeSlot);
    return successJson(booking);
  } catch (err: any) {
    return errorJson(err.message || 'Unable to complete booking.', err.status || 400);
  }
}

export async function OPTIONS() {
  return handleOptions();
}
