import { NextRequest } from 'next/server';
import { loginUser } from '@/lib/db';
import { createAuthToken } from '@/lib/auth';
import { successJson, errorJson, handleOptions } from '@/lib/api-response';

export async function POST(req: NextRequest) {
  try {
    const body = await req.json().catch(() => null);
    if (!body || !body.emailOrPhone || !body.password) {
      return errorJson('Request body is invalid.', 400);
    }
    const user = await loginUser(body.emailOrPhone, body.password);
    const token = createAuthToken(user);
    return successJson({
      success: true,
      token,
      user,
    });
  } catch (err: any) {
    return errorJson(err.message || 'Email/phone or password is incorrect.', err.status || 401);
  }
}

export async function OPTIONS() {
  return handleOptions();
}
