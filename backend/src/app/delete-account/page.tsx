import DeleteAccountClient from './DeleteAccountClient';

export const metadata = {
  title: 'Delete your CourtFinder account',
};

export default function DeleteAccountPage() {
  const googleClientId = process.env.COURTFINDER_GOOGLE_WEB_CLIENT_ID || '';

  return <DeleteAccountClient googleClientId={googleClientId} />;
}
