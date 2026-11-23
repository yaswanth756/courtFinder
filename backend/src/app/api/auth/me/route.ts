import { NextRequest } from 'next/server';
import { verifyAuthToken } from '@/lib/auth';
import { getUser } from '@/lib/db';
import { successJson, errorJson, handleOptions } from '@/lib/api-response';

export async function GET(req: NextRequest) {
  try {
    const authHeader = req.headers.get('authorization');
    const userId = verifyAuthToken(authHeader);
    const user = await getUser(userId);
    return successJson(user);
  } catch (err: any) {
    return errorJson(err.message || 'Sign in to continue.', err.status || 401);
  }
}

export async function OPTIONS() {
  return handleOptions();
}
