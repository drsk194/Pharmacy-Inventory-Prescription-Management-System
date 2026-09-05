import { useEffect, useMemo, useState } from "react";
import Modal from "../common/Modal";
import { batchApi } from "../../api/batchApi";
import { drugApi } from "../../api/drugApi";
import { supplierApi } from "../../api/supplierApi";
import { locationApi } from "../../api/locationApi";
import { useAuth } from "../../hooks/useAuth";
function monthsUntil(value) { if (!value) return null; const target = new Date(value); const now = new Date(); return (target.getFullYear() - now.getFullYear()) * 12 + target.getMonth() - now.getMonth(); }
export default function BatchFormModal({ onClose, onSaved }) { const { user } = useAuth(); const isAdmin = user?.roles?.includes("ROLE_ADMIN");
  const isProcurement = user?.roles?.includes("ROLE_PROCUREMENT_OFFICER"); const [options, setOptions] = useState({ drugs: [], suppliers: [], locations: [] }); const [form, setForm] = useState({ drugId: "", supplierId: "", locationId: "", batchNumber: "", manufacturingDate: "", quantityReceived: "", purchasePrice: "", mrp: "", expiryDate: "" }); const [override, setOverride] = useState(false); const [error, setError] = useState(""); const shortShelfLife = useMemo(() => { const months = monthsUntil(form.expiryDate); return months !== null && months < 6; }, [form.expiryDate]); useEffect(() => {
  Promise.allSettled([
    drugApi.list({ page: 0, size: 200, activeOnly: true }),
    supplierApi.list({ page: 0, size: 200, approvedOnly: true, activeOnly: true }),
    locationApi.list(),
  ]).then(([drugsResult, suppliersResult, locationsResult]) => {
    setOptions({
      drugs: drugsResult.status === "fulfilled" ? (drugsResult.value.data.data.content || drugsResult.value.data.data) : [],
      suppliers: suppliersResult.status === "fulfilled" ? (suppliersResult.value.data.data.content || suppliersResult.value.data.data) : [],
      locations: locationsResult.status === "fulfilled" ? locationsResult.value.data.data : [],
    });
    if (suppliersResult.status === "rejected") setError("Supplier options are not available for this account.");
  });
}, []); function change(event) { setForm({ ...form, [event.target.name]: event.target.value }); } async function submit(event) { event.preventDefault(); if (shortShelfLife && !(isAdmin && override)) { setError(isAdmin ? "Check the override box to accept short shelf life." : "Only an Admin can accept short shelf life."); return; } try { await batchApi.create({ ...form, shortShelfLifeOverrideReason: shortShelfLife ? (override ? "Admin-approved short shelf life" : undefined) : undefined }); onSaved(); } catch (err) { setError(err.response?.data?.message || "Couldn't create batch."); } } return <Modal title="Create Batch" onClose={onClose}><form className="modal-form" onSubmit={submit}>{[["drugId", "Drug", options.drugs], ["supplierId", "Supplier", options.suppliers], ["locationId", "Location", options.locations]].map(([fieldName, label, items]) => <label key={fieldName}>{label}<select name={fieldName} value={form[fieldName]} onChange={change} required={fieldName !== "supplierId" || isAdmin || isProcurement}><option value="">Select...</option>{items.map((item) => <option key={item.id} value={item.id}>{fieldName === "drugId" ? (item.brandName ? `${item.genericName} (${item.brandName})` : item.genericName) : fieldName === "supplierId" ? item.supplierName : item.name}</option>)}</select></label>)}<label>Batch number<input name="batchNumber" value={form.batchNumber} onChange={change} required /></label><label>Manufacturing date<input name="manufacturingDate" type="date" value={form.manufacturingDate} onChange={change} required /></label><label>Quantity received<input name="quantityReceived" type="number" min="1" value={form.quantityReceived} onChange={change} required /></label><label>Purchase price<input name="purchasePrice" type="number" min="0" step="0.01" value={form.purchasePrice} onChange={change} required /></label><label>MRP<input name="mrp" type="number" min="0" step="0.01" value={form.mrp} onChange={change} required /></label><label>Expiry date<input name="expiryDate" type="date" value={form.expiryDate} onChange={change} required /></label>{shortShelfLife && <div className="form-warning">Under six months shelf life.{isAdmin && <label className="checkbox-label"><input type="checkbox" checked={override} onChange={(event) => setOverride(event.target.checked)} />Accept and override</label>}</div>}{error && <p className="form-error">{error}</p>}<button type="submit">Create batch</button></form></Modal>; }
