import { Link } from 'react-router-dom';
import Header from '../components/layout/Header';

const availableItems = [
  { id: 1, name: 'apples', price: 2.49, icon: '🍎', weight: 1 },
  { id: 2, name: 'carrots', price: 1.89, icon: '🥕', weight: 1 },
  { id: 3, name: 'bread', price: 3.25, icon: '🍞', weight: 0.5 },
  { id: 4, name: 'avocados', price: 1.50, icon: '🥑', weight: 1 },
  { id: 5, name: 'bananas', price: 1.29, icon: '🍌', weight: 1 },
  { id: 6, name: 'tomatoes', price: 2.15, icon: '🍅', weight: 1 },
  { id: 7, name: 'milk', price: 4.10, icon: '🥛', weight: 3.5 },
  { id: 8, name: 'lettuce', price: 2.75, icon: '🥬', weight: 1.5 },
];

const cartPreview = [
  { id: 1, name: 'apples', quantity: 2 },
  { id: 2, name: 'bread', quantity: 2 },
  { id: 3, name: 'carrots', quantity: 1 },
];

function CartPage() {
  const aggregatePrice = cartPreview.reduce((total, cartItem) => {
    const product = availableItems.find(
      (item) => item.name == cartItem.name
    );
    const finalPrice = cartItem.quantity * product.price;
    return total + finalPrice;
  }, 0);

  return (<section>
      <Header />

      <h1>my cart</h1>


      {cartPreview.map((cartItem) => {
          const product = availableItems.find(
          (item) => item.name === cartItem.name
          );
          
          const finalWeight = cartItem.quantity * product.weight;

          return (
          <p key = {cartItem.id}>
            {cartItem.name}: {cartItem.quantity} item(s), weight = {finalWeight}
            </p>
        );
      })}


      <p>Total Price: ${aggregatePrice.toFixed(2)}</p>

      <div>
        <Link to="/shop">Back to Shop</Link>
        <Link to="/checkout"> Go to Checkout</Link>
      </div>
    </section>);
}

export default CartPage;
