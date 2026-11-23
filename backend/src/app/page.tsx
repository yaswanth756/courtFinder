import Link from 'next/link';

export default function HomePage() {
  return (
    <main
      style={{
        minHeight: '100vh',
        backgroundColor: '#101820',
        color: '#f5f7fa',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '24px',
        textAlign: 'center',
      }}
    >
      <div
        style={{
          maxWidth: '560px',
          width: '100%',
          backgroundColor: '#1b2935',
          border: '1px solid #344451',
          borderRadius: '20px',
          padding: '36px 28px',
          boxShadow: '0 8px 30px rgba(0,0,0,0.4)',
        }}
      >
        <div style={{ fontSize: '3rem', marginBottom: '16px' }}>🎾</div>
        <h1 style={{ margin: '0 0 12px 0', fontSize: '1.8rem', fontWeight: 700 }}>
          CourtFinder Backend
        </h1>
        <p style={{ color: '#a0aec0', lineHeight: 1.6, marginBottom: '24px' }}>
          Next.js cloud backend for the CourtFinder Android application, deployed on Vercel.
        </p>
        <div
          style={{
            display: 'inline-block',
            padding: '8px 16px',
            borderRadius: '9999px',
            backgroundColor: 'rgba(72, 187, 120, 0.2)',
            color: '#48bb78',
            fontWeight: 600,
            fontSize: '0.9rem',
            marginBottom: '28px',
          }}
        >
          ● Status: Operational
        </div>
        <div style={{ borderTop: '1px solid #344451', paddingTop: '20px' }}>
          <p style={{ color: '#cbd5e0', fontSize: '0.9rem' }}>Required endpoints:</p>
          <ul style={{ listStyle: 'none', padding: 0, margin: '12px 0', textAlign: 'left', fontSize: '0.85rem', color: '#a0aec0' }}>
            <li><code>GET  /api/health</code> - Service health check</li>
            <li><code>POST /api/auth/register</code> - Register new player</li>
            <li><code>POST /api/auth/login</code> - Player authentication</li>
            <li><code>POST /api/auth/google</code> - Google OAuth & Firebase sign-in</li>
            <li><code>GET  /api/courts/search</code> - Live court listings</li>
            <li><code>POST /api/bookings</code> - Slot reservations</li>
            <li><code>GET  /delete-account</code> - Account deletion portal</li>
          </ul>
        </div>
      </div>
    </main>
  );
}
