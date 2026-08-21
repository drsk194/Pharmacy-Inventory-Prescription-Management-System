import { useEffect, useRef } from "react";

export default function Modal({ title, onClose, children }) {
  const modalRef = useRef(null);
  const previouslyFocusedElement = useRef(null);

  useEffect(() => {
    previouslyFocusedElement.current = document.activeElement;
    modalRef.current?.focus();
    const onKeyDown = (event) => event.key === "Escape" && onClose();
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("keydown", onKeyDown);
      previouslyFocusedElement.current?.focus?.();
    };
  }, [onClose]);

  return <div className="modal-overlay" onClick={onClose}>
    <div className="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title" tabIndex={-1} ref={modalRef} onClick={(event) => event.stopPropagation()}>
      <header className="modal__header"><h2 id="modal-title">{title}</h2><button type="button" onClick={onClose} aria-label="Close">X</button></header>
      <div className="modal__body">{children}</div>
    </div>
  </div>;
}
