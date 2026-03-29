import { render, screen } from "@testing-library/react";
import { AppShell } from "@/components/app-shell";

describe("AppShell", () => {
  it("renders desktop and mobile navigation labels", () => {
    render(<AppShell title="Gachamarket">content</AppShell>);

    expect(screen.getByText("경기")).toBeInTheDocument();
    expect(screen.getByText("리더보드")).toBeInTheDocument();
    expect(screen.getByText("프로필")).toBeInTheDocument();
  });
});
