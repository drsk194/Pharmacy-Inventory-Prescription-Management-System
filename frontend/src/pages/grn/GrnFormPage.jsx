import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { grnApi } from "../../api/grnApi";
import { purchaseOrderApi } from "../../api/purchaseOrderApi";
import { locationApi } from "../../api/locationApi";

function todayLocalDateTime() {
  const now = new Date();
  now.setSeconds(0, 0);
  return now.toISOString().slice(0, 16);
}

export default function GrnFormPage() {
  const [pos, setPos] = useState([]);
  const [locations, setLocations] = useState([]);
  const [po, setPo] = useState(null);
  const [poId, setPoId] = useState("");
  const [receivedDate, setReceivedDate] = useState(todayLocalDateTime());
  const [items, setItems] = useState({});
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    Promise.all([purchaseOrderApi.list({ page: 0, size: 200, status: "APPROVED" }), locationApi.list()])
      .then(([poResponse, locationResponse]) => {
        const poData = poResponse.data.data;
        setPos(poData.content || poData);
        setLocations(locationResponse.data.data);
      })
      .catch(() => setError("Could not load approved POs and locations."));
  }, []);

  async function selectPo(id) {
    setPoId(id);
    if (!id) { setPo(null); return; }
    try {
      const response = await purchaseOrderApi.getById(id);
      const data = response.data.data;
      setPo(data);
      const initial = {};
      (data.items || []).forEach((item) => {
        initial[item.id] = { batchNumber: "", manufacturingDate: "", expiryDate: "", receivedQuantity: item.orderedQuantity, purchasePrice: item.unitPrice || "", mrp: "", locationId: "" };
      });
      setItems(initial);
    } catch (err) {
      setError(err.response?.data?.message || "Could not load PO.");
    }
  }

  function update(id, field, value) {
    setItems({ ...items, [id]: { ...items[id], [field]: value } });
  }

  async function submit(event) {
    event.preventDefault();
    setError("");
    try {
      await grnApi.create({
        purchaseOrderId: Number(poId),
        receivedDate: receivedDate ? `${receivedDate}:00` : undefined,
        items: Object.entries(items).map(([purchaseOrderItemId, details]) => ({
          purchaseOrderItemId: Number(purchaseOrderItemId),
          ...details,
          receivedQuantity: Number(details.receivedQuantity),
          purchasePrice: Number(details.purchasePrice),
          mrp: Number(details.mrp),
          locationId: Number(details.locationId),
        })),
      });
      navigate("/grn");
    } catch (err) {
      setError(err.response?.data?.message || "Could not submit GRN.");
    }
  }

  return (
    <main className="detail-page">
      <h1>New goods receipt</h1>
      <form className="modal-form modal-form--standalone" onSubmit={submit}>
        <label>
          Approved purchase order
          <select value={poId} onChange={(event) => selectPo(event.target.value)} required>
            <option value="">Select...</option>
            {pos.map((item) => <option key={item.id} value={item.id}>PO #{item.id} - {item.supplierName}</option>)}
          </select>
        </label>
        <label>
          Received date
          <input type="datetime-local" value={receivedDate} max={todayLocalDateTime()} onChange={(event) => setReceivedDate(event.target.value)} required />
        </label>
        {po && (po.items || []).map((item) => (
          <div className="grn-item-block" key={item.id}>
            <strong>{item.drugGenericName} - ordered {item.orderedQuantity}</strong>
            <label>Batch number<input type="text" value={items[item.id]?.batchNumber || ""} onChange={(event) => update(item.id, "batchNumber", event.target.value)} required /></label>
            <label>Manufacturing date<input type="date" value={items[item.id]?.manufacturingDate || ""} onChange={(event) => update(item.id, "manufacturingDate", event.target.value)} required /></label>
            <label>Expiry date<input type="date" value={items[item.id]?.expiryDate || ""} onChange={(event) => update(item.id, "expiryDate", event.target.value)} required /></label>
            <label>Received quantity<input type="number" min="0" step="0.01" value={items[item.id]?.receivedQuantity || ""} onChange={(event) => update(item.id, "receivedQuantity", event.target.value)} required /></label>
            <label>Purchase price<input type="number" min="0" step="0.01" value={items[item.id]?.purchasePrice || ""} onChange={(event) => update(item.id, "purchasePrice", event.target.value)} required /></label>
            <label>MRP<input type="number" min="0" step="0.01" value={items[item.id]?.mrp || ""} onChange={(event) => update(item.id, "mrp", event.target.value)} required /></label>
            <label>Location<select value={items[item.id]?.locationId || ""} onChange={(event) => update(item.id, "locationId", event.target.value)} required>
              <option value="">Select...</option>
              {locations.map((location) => <option key={location.id} value={location.id}>{location.name}</option>)}
            </select></label>
          </div>
        ))}
        {error && <p className="form-error">{error}</p>}
        <button type="submit" disabled={!po}>Submit GRN</button>
      </form>
    </main>
  );
}
