import { useState } from 'react';

function CreateProductForm( { onCreate } ) {
    const [isOpen, setIsOpen] = useState(false);
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [weight, setWeight] = useState('');
    const [price, setPrice] = useState('');
    const [imageUrl, setImageUrl] = useState('');
    const [quantity, setQuantity] = useState('');

    function resetForm() {
        setName('');
        setDescription('');
        setWeight('');
        setPrice('');
        setImageUrl('');
        setQuantity('');
        setIsOpen(false);
    }

    async function handleSubmit(event) {
        event.preventDefault();
        const added = await onCreate(
            {
                name: name.trim(),
                description: description.trim() || null,
                unitWeight: Number(weight),
                unitPrice: Number(price),
                imageUrl: imageUrl.trim() || null,
                quantity: Number(quantity),
            }
        );
        if (added) resetForm();
    }

    //this will be the button when the form is closed (will be sitting under product search bar)
    if (!isOpen) {
        return (
            <button type="button" onClick={() => setIsOpen(true)}
                className="w-full rounded-2xl border-2 border-dashed border-brand-green-500 p-4 font-display font-bold text-brand-green-700 hover:bg-brand-green-50">
                Create Product
            </button>
        )
    }

    return (
        <form onSubmit={handleSubmit}
            className="flex flex-wrap items-end gap-3 rounded-2xl border-2 border-brand-green-500 bg-white p-4">
            <label className="flex flex-1 flex-col text-sm font-bold text-ink">
                Name
                <input required value={name} onChange={(e) => setName(e.target.value)}
                    className="mt-1 rounded border border-brand-green-100 px-2 py-1 font-normal" />
            </label>

            <label className="flex w-24 flex-col text-sm font-bold text-ink">
                Price ($)
                <input required type="number" min="0.01" step="0.01" value={price} onChange={(e) => setPrice(e.target.value)}
                    className="mt-1 rounded border border-brand-green-100 px-2 py-1 font-normal" />
            </label>

            <label className="flex w-24 flex-col text-sm font-bold text-ink">
                Weight (lb)
                <input required type="number" min="0.001" step="0.001" value={weight} onChange={(e) => setWeight(e.target.value)}
                    className="mt-1 rounded border border-brand-green-100 px-2 py-1 font-normal" />
            </label>

            <label className="flex w-24 flex-col text-sm font-bold text-ink">
                Stock
                <input required type="number" min="0" step="1" value={quantity} onChange={(e) => setQuantity(e.target.value)}
                    className="mt-1 rounded border border-brand-green-100 px-2 py-1 font-normal" />
            </label>

            <label className="flex w-full flex-col text-sm font-bold text-ink">
                Description
                <textarea maxLength={1000} rows={2} value={description} onChange={(e) => setDescription(e.target.value)}
                    className="mt-1 rounded border border-brand-green-100 px-2 py-1 font-normal" />
            </label>

            <label className="flex w-full flex-col text-sm font-bold text-ink">
                Image URL
                <input type="url" maxLength={5000} placeholder="https://..." value={imageUrl} onChange={(e) => setImageUrl(e.target.value)}
                    className="mt-1 rounded border border-brand-green-100 px-2 py-1 font-normal" />
            </label>

            <div className="flex gap-2">
                <button type="button" onClick={resetForm}
                    className="rounded-lg border-2 border-brand-green-500 px-4 py-2 font-bold text-brand-green-700 hover:bg-brand-green-50">
                    Cancel
                </button>
                <button type="submit"
                    className="rounded-lg bg-brand-green-600 px-4 py-2 font-bold text-white hover:bg-brand-green-700">
                    Create
                </button>
            </div>
        </form>
    )
}

export default CreateProductForm;