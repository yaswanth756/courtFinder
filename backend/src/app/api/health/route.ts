import { successJson, handleOptions } from '@/lib/api-response';

export async function GET() {
  return successJson({ status: 'ok' });
}

export async function OPTIONS() {
  return handleOptions();
}
