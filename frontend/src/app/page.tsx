import Link from "next/link";
import { AppShell } from "@/components/app-shell";

const categories = [
  { name: "축구", description: "EPL, 챔피언스리그, 국내 컵 대회" },
  { name: "야구", description: "KBO, MLB 주요 경기 예측" },
  { name: "농구", description: "NBA, 국제 대회 하이라이트" },
];

export default async function HomePage() {
  const apiBaseUrl = process.env.API_BASE_URL ?? "http://localhost:8080";

  return (
    <AppShell title="Gachamarket">
      <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {categories.map((category) => (
            <article
              key={category.name}
              className="rounded-[2rem] border border-white/10 bg-white/6 p-6 shadow-[0_24px_80px_rgba(0,0,0,0.28)]"
            >
              <p className="text-xs uppercase tracking-[0.35em] text-cyan-300">
                Category
              </p>
              <h2 className="mt-3 text-2xl font-semibold text-white">
                {category.name}
              </h2>
              <p className="mt-3 text-sm leading-6 text-slate-300">
                {category.description}
              </p>
            </article>
          ))}
        </section>
        <aside className="rounded-[2rem] border border-cyan-400/30 bg-cyan-400/10 p-6">
          <h2 className="text-lg font-semibold text-white">지금 바로 시작</h2>
          <p className="mt-3 text-sm leading-6 text-slate-100">
            가입 즉시 1,000P를 받고 공개 카테고리에서 예측을 시작할 수 있습니다.
          </p>
          <Link
            href={`${apiBaseUrl}/oauth2/authorization/google`}
            className="mt-5 inline-flex rounded-full bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:bg-cyan-100"
          >
            Google로 로그인
          </Link>
        </aside>
      </div>
    </AppShell>
  );
}
