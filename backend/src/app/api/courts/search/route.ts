import { NextRequest } from 'next/server';
import { searchCourts } from '@/lib/db';
import { successJson, errorJson, handleOptions } from '@/lib/api-response';

export async function GET(req: NextRequest) {
  try {
    const { searchParams } = new URL(req.url);
    const query = searchParams.get('query') || '';
    const courts = await searchCourts(query);
    return successJson(courts);
  } catch (err: any) {
    return errorJson(err.message || 'Failed to search courts.', err.status || 500);
  }
}

export async function OPTIONS() {
  return handleOptions();
}
