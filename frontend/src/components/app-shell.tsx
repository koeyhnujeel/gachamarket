type Props = {
  title: string;
  children: React.ReactNode;
};

export function AppShell({ title, children }: Props) {
  return (
    <div className="min-h-screen text-white">
      <header className="border-b border-white/10 backdrop-blur-sm">
        <div className="mx-auto flex max-w-6xl flex-col gap-4 px-4 py-5 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="font-mono text-[11px] uppercase tracking-[0.45em] text-cyan-300">
              Predict Better
            </p>
            <h1 className="mt-2 text-2xl font-semibold tracking-tight">
              {title}
            </h1>
          </div>
          <nav className="flex gap-3 text-sm text-slate-300">
            <span className="rounded-full border border-white/10 bg-white/5 px-3 py-2">
              경기
            </span>
            <span className="rounded-full border border-white/10 bg-white/5 px-3 py-2">
              리더보드
            </span>
            <span className="rounded-full border border-white/10 bg-white/5 px-3 py-2">
              프로필
            </span>
          </nav>
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl px-4 py-8">{children}</main>
    </div>
  );
}
