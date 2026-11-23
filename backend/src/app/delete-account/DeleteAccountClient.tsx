'use client';

import React, { useState, useEffect } from 'react';
import Script from 'next/script';

export default function DeleteAccountClient({ googleClientId }: { googleClientId: string }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [status, setStatus] = useState('');
  const [busy, setBusy] = useState(false);
  const [deleted, setDeleted] = useState(false);

  async function sendJson(path: string, body: any) {
    const response = await fetch(path, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new Error(data.errorMessage || 'Sign-in failed. Check your details and try again.');
    }
    return data;
  }

  async function deleteWithToken(token: string) {
    const response = await fetch('/api/account', {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!response.ok) {
      const data = await response.json().catch(() => ({}));
      throw new Error(data.errorMessage || 'Account deletion failed. Please try again.');
    }
    setStatus('Your CourtFinder account and booking records have been deleted.');
    setDeleted(true);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setStatus('Processing deletion…');
    try {
      const account = await sendJson('/api/auth/login', {
        emailOrPhone: email,
        password,
      });
      await deleteWithToken(account.token);
    } catch (err: any) {
      setStatus(err.message || 'An error occurred.');
    } finally {
      setBusy(false);
    }
  }

  useEffect(() => {
    if (!googleClientId || deleted) return;

    (window as any).onGoogleCredential = async (response: any) => {
      setBusy(true);
      setStatus('Verifying your Google account…');
      try {
        const account = await sendJson('/api/auth/google', { idToken: response.credential });
        await deleteWithToken(account.token);
      } catch (err: any) {
        setStatus(err.message || 'Google verification failed.');
      } finally {
        setBusy(false);
      }
    };
  }, [googleClientId, deleted]);

  return (
    <div
      style={{
        minHeight: '100vh',
        backgroundColor: '#101820',
        color: '#f5f7fa',
        display: 'grid',
        placeItems: 'center',
        padding: '24px',
        boxSizing: 'border-box',
      }}
    >
      <Script src="https://accounts.google.com/gsi/client" strategy="afterInteractive" />
      <main
        style={{
          width: 'min(100%, 480px)',
          backgroundColor: '#1b2935',
          border: '1px solid #344451',
          borderRadius: '20px',
          padding: '28px',
          boxSizing: 'border-box',
        }}
      >
        <h1 style={{ marginTop: 0, fontSize: '1.6rem' }}>Delete your CourtFinder account</h1>
        <p style={{ color: '#d1d9df', lineHeight: 1.55 }}>
          Sign in to permanently delete your CourtFinder account and associated booking records. This action cannot be undone.
        </p>

        {!deleted && (
          <>
            <form onSubmit={handleSubmit}>
              <label style={{ display: 'block', margin: '14px 0 6px' }} htmlFor="email">
                Email address
              </label>
              <input
                id="email"
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                style={{
                  width: '100%',
                  padding: '12px',
                  boxSizing: 'border-box',
                  borderRadius: '8px',
                  border: '1px solid #60717d',
                  backgroundColor: '#101820',
                  color: 'white',
                }}
              />

              <label style={{ display: 'block', margin: '14px 0 6px' }} htmlFor="password">
                Password
              </label>
              <input
                id="password"
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{
                  width: '100%',
                  padding: '12px',
                  boxSizing: 'border-box',
                  borderRadius: '8px',
                  border: '1px solid #60717d',
                  backgroundColor: '#101820',
                  color: 'white',
                }}
              />

              <button
                type="submit"
                disabled={busy}
                style={{
                  marginTop: '16px',
                  padding: '12px 16px',
                  border: 0,
                  borderRadius: '8px',
                  backgroundColor: '#b3261e',
                  color: 'white',
                  fontWeight: 700,
                  cursor: busy ? 'wait' : 'pointer',
                  opacity: busy ? 0.6 : 1,
                  width: '100%',
                }}
              >
                {busy ? 'Processing…' : 'Sign in and delete account'}
              </button>
            </form>

            {googleClientId && (
              <>
                <div style={{ borderTop: '1px solid #344451', margin: '24px 0' }} />
                <p style={{ color: '#d1d9df' }}>You can also sign in with the Google account linked to CourtFinder:</p>
                <div
                  id="g_id_onload"
                  data-client_id={googleClientId}
                  data-callback="onGoogleCredential"
                />
                <div className="g_id_signin" data-type="standard" />
              </>
            )}
          </>
        )}

        {status && (
          <p
            role="status"
            style={{
              marginTop: '16px',
              padding: '12px',
              borderRadius: '8px',
              backgroundColor: deleted ? 'rgba(72, 187, 120, 0.2)' : 'rgba(239, 68, 68, 0.2)',
              color: deleted ? '#48bb78' : '#f87171',
            }}
          >
            {status}
          </p>
        )}
      </main>
    </div>
  );
}
