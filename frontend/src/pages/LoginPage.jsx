import AuthForm from "../components/auth/AuthForm";

function LoginPage() {
  const pageTitle = "Login";
  return (
  <section>
    <AuthForm pageTitle={pageTitle}/>
  </section>);
}

export default LoginPage;
