import { AppShell } from "@/components/app-shell";

export default function HomePage() {
  return (
    <AppShell title="Gachamarket">
      <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
        <section className="rounded-[2rem] border border-white/10 bg-white/6 p-6 shadow-[0_24px_80px_rgba(0,0,0,0.28)]">
          <p className="text-xs uppercase tracking-[0.4em] text-cyan-300">
            Public Lobby
          </p>
          <h2 className="mt-3 text-3xl font-semibold text-white">
            카테고리 허브
          </h2>
          <p className="mt-3 max-w-xl text-sm leading-6 text-slate-300">
            로그인 전에도 스포츠 카테고리와 향후 공개 이벤트 진입 구조를 볼 수
            있는 기본 셸입니다.
          </p>
        </section>
        <aside className="rounded-[2rem] border border-cyan-400/30 bg-cyan-400/10 p-6">
          <h2 className="text-lg font-semibold text-white">빠른 이동</h2>
          <ul className="mt-4 space-y-2 text-sm text-slate-200">
            <li>축구</li>
            <li>야구</li>
            <li>농구</li>
          </ul>
        </aside>
      </div>
    </AppShell>
  );
}
