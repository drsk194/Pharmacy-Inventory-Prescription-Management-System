import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { purchaseOrderApi } from "../../api/purchaseOrderApi";
import { supplierApi } from "../../api/supplierApi";
import { drugApi } from "../../api/drugApi";
import PriceComparisonModal from "../../components/purchase-orders/PriceComparisonModal";

function tomorrow() {
  const date = new Date();
  date.setDate(date.getDate() + 1);
  return date.toISOString().slice(0, 10);
}

export default function PurchaseOrderFormPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [suppliers, setSuppliers] = useState([]);
  const [drugs, setDrugs] = useState([]);
  const prefill = location.state?.prefillItem;
  const [supplierId, setSupplierId] = useState("");
  const [expectedDeliveryDate, setExpectedDeliveryDate] = useState(tomorrow());
  const [deliveryTerms, setDeliveryTerms] = useState("");
  const [items, setItems] = useState([{ drugId: prefill?.drugId || "", orderedQuantity: prefill?.quantity || "", unitPrice: "" }]);
  const [compare, setCompare] = useState(null);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    Promise.all([
      supplierApi.list({ page: 0, size: 200, approvedOnly: true, activeOnly: true }),
      drugApi.list({ page: 0, size: 500, activeOnly: true }),
    ]).then(([suppliersResponse, drugsResponse]) => {
      const supplierData = suppliersResponse.data.data;
      const drugData = drugsResponse.data.data;
      setSuppliers(supplierData.content || supplierData);
      setDrugs(drugData.content || drugData);
    }).catch(() => setError("Could not load PO options."));
  }, []);

  function update(index, field, value) {
    setItems(items.map((item, itemIndex) => (itemIndex === index ? { ...item, [field]: value } : item)));
  }

  function addRow() {
    setItems([...items, { drugId: "", orderedQuantity: "", unitPrice: "" }]);
  }

  function removeRow(index) {
    setItems(items.filter((_, itemIndex) => itemIndex !== index));
  }

  async function submit(event) {
    event.preventDefault();
    setError("");
    setSaving(true);
    const cleanItems = items.map((item) => ({
      drugId: Number(item.drugId),
      orderedQuantity: Number(item.orderedQuantity),
      unitPrice: Number(item.unitPrice),
    }));
    try {
      const response = await purchaseOrderApi.create({
        supplierId: Number(supplierId),
        expectedDeliveryDate,
        deliveryTerms: deliveryTerms || undefined,
        items: [cleanItems[0]],
      });
      const id = response.data.data.id;
      for (const item of cleanItems.slice(1)) {
        await purchaseOrderApi.addItem(id, item);
      }
      navigate(`/purchase-orders/${id}`);
    } catch (err) {
      setError(err.response?.data?.message || "Could not create purchase order.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <main className="detail-page">
      <h1>New purchase order</h1>
      <form className="modal-form modal-form--standalone" onSubmit={submit}>
        <label>
          Supplier (approved only)
          <select value={supplierId} onChange={(event) => setSupplierId(event.target.value)} required>
            <option value="">Select...</option>
            {suppliers.map((supplier) => <option key={supplier.id} value={supplier.id}>{supplier.supplierName}</option>)}
          </select>
        </label>
        <label>
          Expected delivery date
          <input type="date" value={expectedDeliveryDate} min={tomorrow()} onChange={(event) => setExpectedDeliveryDate(event.target.value)} required />
        </label>
        <label>
          Delivery terms (optional)
          <input value={deliveryTerms} onChange={(event) => setDeliveryTerms(event.target.value)} placeholder="e.g. FOB destination" />
        </label>
        <h2>Items</h2>
        {items.map((item, index) => (
          <div className="prescription-item-row" key={index}>
            <select value={item.drugId} onChange={(event) => update(index, "drugId", event.target.value)} required>
              <option value="">Select drug...</option>
              {drugs.map((drug) => <option key={drug.id} value={drug.id}>{drug.brandName ? `${drug.genericName} (${drug.brandName})` : drug.genericName}</option>)}
            </select>
            <input type="number" min="1" placeholder="Qty" value={item.orderedQuantity} onChange={(event) => update(index, "orderedQuantity", event.target.value)} required />
            <input type="number" min="0" step="0.01" placeholder="Unit price" value={item.unitPrice} onChange={(event) => update(index, "unitPrice", event.target.value)} required />
            {item.drugId && <button type="button" onClick={() => setCompare({ id: item.drugId, name: drugs.find((drug) => String(drug.id) === String(item.drugId))?.genericName })}>Compare prices</button>}
            {items.length > 1 && <button type="button" className="button--outline" onClick={() => removeRow(index)}>Remove</button>}
          </div>
        ))}
        <button type="button" className="button--outline" onClick={addRow}>Add another item</button>
        {error && <p className="form-error">{error}</p>}
        <button type="submit" disabled={saving}>{saving ? "Creating…" : "Create purchase order"}</button>
      </form>
      {compare && <PriceComparisonModal drugId={compare.id} drugName={compare.name} onClose={() => setCompare(null)} />}
    </main>
  );
}
