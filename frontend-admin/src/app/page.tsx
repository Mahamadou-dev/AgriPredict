import HealthCheck from '../components/HealthCheck';

export default function Home() {
  return (
    <main className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-green-800 mb-8">
          AgriPredict - Dashboard Admin
        </h1>
        <HealthCheck />
      </div>
    </main>
  );
}