import { Link } from 'react-router-dom'
import OrderStatusTable from '../components/manager/OrderStatusTable'

function ManagerOrdersPage() {
    return (
        <main className="mx-auto w-full max-w-5xl p-4 sm:p-6">
            <Link to="/manager" className="text-sm font-bold text-brand-green-700 hover:underline">
                Back to products
            </Link>

            <h1 className="mt-2 font-display text-3xl font-black text-ink">Orders Dashboard</h1>
            <p className="mt-1 text-sm text-ink/75">View all customer orders, update statuses</p>

            <OrderStatusTable />
        </main>
    );
}

export default ManagerOrdersPage;