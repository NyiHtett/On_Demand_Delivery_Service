import { Link } from 'react-router-dom';
import notFoundImage from '../assets/not-found.svg';

function NotFoundPage() {
  return (
    <main className="grid min-h-screen place-items-center bg-paper px-6 py-10">
      <div className="flex flex-col items-center">
        <img
          src={notFoundImage}
          alt="404 not found"
          className="w-full max-w-md"
        />
        <Link
          to="/"
          className="mt-6 text-sm font-bold lowercase text-brand-green-700 underline decoration-brand-orange-400 decoration-2 underline-offset-4 hover:text-brand-orange-500 focus-visible:outline-3 focus-visible:outline-offset-4 focus-visible:outline-brand-green-500"
        >
          return home
        </Link>
      </div>
    </main>
  );
}

export default NotFoundPage;
