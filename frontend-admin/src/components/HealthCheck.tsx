'use client';

import { useState, useEffect } from 'react';

interface WeatherForecast {
  date: string;
  temperatureC: number;
  temperatureF: number;
  summary: string;
}

export default function HealthCheck() {
  const [weatherData, setWeatherData] = useState<WeatherForecast[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const testBackendConnection = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // URL COMPLÈTE avec le bon endpoint
      const apiUrl = `${process.env.NEXT_PUBLIC_API_URL}/weatherforecast`;
      console.log('Testing URL:', apiUrl);
      
      const response = await fetch(apiUrl);
      
      if (!response.ok) {
        throw new Error(`Backend returned status: ${response.status}`);
      }
      
      const data = await response.json();
      setWeatherData(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error occurred');
      console.error('Connection error:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    testBackendConnection();
  }, []);

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-xl font-semibold mb-4">Test de Connexion Backend</h2>
      
      <button
        onClick={testBackendConnection}
        disabled={loading}
        className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 disabled:bg-gray-400 mb-4"
      >
        {loading ? 'Test en cours...' : 'Tester la connexion'}
      </button>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          <strong>Erreur:</strong> {error}
          <br />
          <span className="text-sm">
            Vérifie que le backend est accessible à: {process.env.NEXT_PUBLIC_API_URL}/weatherforecast
          </span>
        </div>
      )}

      {weatherData.length > 0 && (
        <div>
          <h3 className="font-medium mb-2 text-green-600">✅ Connexion backend réussie !</h3>
          <div className="grid gap-2">
            {weatherData.map((forecast, index) => (
              <div key={index} className="border p-3 rounded">
                <p><strong>Date:</strong> {forecast.date}</p>
                <p><strong>Température:</strong> {forecast.temperatureC}°C ({forecast.temperatureF}°F)</p>
                <p><strong>Résumé:</strong> {forecast.summary}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="mt-4 text-sm text-gray-600">
        <p><strong>URL Backend configurée:</strong> {process.env.NEXT_PUBLIC_API_URL}</p>
        <p><strong>Endpoint testé:</strong> /weatherforecast</p>
        <p><strong>Environnement:</strong> {process.env.NODE_ENV}</p>
      </div>
    </div>
  );
}