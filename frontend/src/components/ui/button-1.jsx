function GradientButton({
  children,
  width = '600px',
  height = '100px',
  className = '',
  onClick,
  disabled = false,
  ...props
}) {
  function handleKeyDown(event) {
    if (disabled) return;
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      onClick?.();
    }
  }

  return (
    <div className="text-center text-paper">
      <div
        role="button"
        tabIndex={disabled ? -1 : 0}
        className={`relative flex cursor-pointer items-center justify-center overflow-hidden rounded-[50px] border-2 border-brand-orange-400 bg-brand-orange-400 shadow-md transition duration-300 after:absolute after:inset-[4px] after:rounded-[45px] after:bg-brand-orange-400 after:content-[''] hover:scale-105 hover:border-white hover:bg-brand-orange-500 hover:shadow-lg ${
          disabled ? 'cursor-not-allowed opacity-50' : ''
        } ${className}`}
        style={{ minWidth: width, height }}
        onClick={disabled ? undefined : onClick}
        onKeyDown={handleKeyDown}
        aria-disabled={disabled}
        {...props}
      >
        <span className="relative z-10 flex items-center justify-center text-sm font-bold text-brand-green-700">
          {children}
        </span>
      </div>
    </div>
  );
}

export default GradientButton;
