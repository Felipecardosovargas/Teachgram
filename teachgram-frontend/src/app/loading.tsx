import Image from 'next/image';

export default function Loading() {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-var(--color-primary)">
      <Image
        src="/loader.png"
        alt="Loader"
        width={80}
        height={80}
      />
      <p className="mt-4 text-lg text-white">Carregando...</p>
    </div>
  );
}
