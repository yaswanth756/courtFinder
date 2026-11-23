import { NextRequest } from 'next/server';
import { registerUser } from '@/lib/db';
import { createAuthToken } from '@/lib/auth';
import { successJson, errorJson, handleOptions } from '@/lib/api-response';

export async function POST(req: NextRequest) {
  try {
    const body = await req.json().catch(() => null);
    if (!body || !body.fullName || !body.email || !body.password) {
      return errorJson('Request body is invalid.', 400);
    }
    const user = await registerUser(body.fullName, body.email, body.phone || '', body.password);
    const token = createAuthToken(user);
    return successJson({
      success: true,
      token,
      user,
    });
  } catch (err: any) {
    return errorJson(err.message || 'The server could not complete the request.', err.status || 500);
  }
}

export async function OPTIONS() {
  return handleOptions();
}
