{/* This basically uses the product lines to create a table that the users can see, passes the product and inc/dec functions to the line*/}

import ProductLine from './ProductLine';
 
function ProductTable({ products, onDecrease, onIncrease, onWeightChange, onPriceChange, onStockChange, onDescriptionChange, onImageUrlChange, onDelete }) {
  return (
    <ul className="flex flex-col gap-3">
      {products.map((product) => (
        <li key={product.id}>
          <ProductLine
            product={product}
            onDecrease={() => onDecrease(product.id)}
            onIncrease={() => onIncrease(product.id)}
            onWeightChange={(value) => onWeightChange(product.id, value)}
            onPriceChange={(value) => onPriceChange(product.id, value)}
            onStockChange={(value) => onStockChange(product.id, value)}
            onDescriptionChange={(value) => onDescriptionChange(product.id, value)}
            onImageUrlChange={(value) => onImageUrlChange(product.id, value)}
            onDelete = {() => onDelete(product.id)}
          />
        </li>
      ))}
    </ul>
  );
}
 
export default ProductTable;