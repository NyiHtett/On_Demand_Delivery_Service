import ProductCard from './ProductCard';

function ProductGrid({ products, quantities, onDecrease, onIncrease, onAddToCart }) {
  return (
    <ul className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4" aria-label="Available products">
      {products.map((product) => (
        <li key={product.id}>
          <ProductCard
            product={product}
            quantity={quantities[product.id] ?? 0}
            onDecrease={() => onDecrease(product.id)}
            onIncrease={() => onIncrease(product.id, product.stock)}
            onAddToCart={() => onAddToCart(product.id)}
          />
        </li>
      ))}
    </ul>
  );
}

export default ProductGrid;
