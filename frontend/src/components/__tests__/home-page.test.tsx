import { render, screen } from "@testing-library/react";
import HomePage from "@/app/page";

describe("HomePage", () => {
  it("shows category cards and login CTA", async () => {
    render(await HomePage());

    expect(screen.getByText("축구")).toBeInTheDocument();
    expect(screen.getByText("야구")).toBeInTheDocument();
    expect(screen.getByText("Google로 로그인")).toBeInTheDocument();
  });
});
