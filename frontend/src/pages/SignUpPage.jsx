import AuthForm from "../components/auth/AuthForm";

function SignUpPage() {
  const pageTitle = "Sign Up";
  return (
  <section>
    <AuthForm pageTitle={pageTitle}/>
  </section>);
}

export default SignUpPage;
