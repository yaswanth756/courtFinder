import { NextRequest, NextResponse } from 'next/server';
import { verifyAuthToken } from '@/lib/auth';
import { deleteAccount } from '@/lib/db';
import { errorJson, handleOptions, corsHeaders } from '@/lib/api-response';

export async function DELETE(req: NextRequest) {
  try {
    const authHeader = req.headers.get('authorization');
    const userId = verifyAuthToken(authHeader);
    await deleteAccount(userId);
    return new NextResponse(null, {
      status: 204,
      headers: corsHeaders,
    });
  } catch (err: any) {
    return errorJson(err.message || 'Sign in before deleting your account.', err.status || 401);
  }
}

export async function OPTIONS() {
  return handleOptions();
}
