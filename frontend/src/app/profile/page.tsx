import { AppShell } from "@/components/app-shell";

export default function ProfilePage() {
  return (
    <AppShell title="내 프로필">
      <section className="rounded-[2rem] border border-white/10 bg-white/6 p-6 shadow-[0_24px_80px_rgba(0,0,0,0.28)]">
        <h2 className="text-2xl font-semibold text-white">프로필 기본 구조</h2>
        <p className="mt-3 text-sm leading-6 text-slate-300">
          포인트, 닉네임, 활성 칭호를 이 영역에 배치할 수 있도록 셸을 먼저
          준비했습니다.
        </p>
      </section>
    </AppShell>
  );
}
