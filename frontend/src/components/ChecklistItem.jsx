import { updateChecklistItem } from '../api/client';

export default function ChecklistItem({ item, onItemUpdated }) {
  async function handleChange(event) {
    const isPacked = event.target.checked;
    const updated = await updateChecklistItem(item.id, isPacked);
    onItemUpdated(updated);
  }

  return (
    <li className={`checklist-item${item.isPacked ? ' is-packed' : ''}`}>
      <label className="checklist-item-label">
        <input
          type="checkbox"
          checked={item.isPacked}
          onChange={handleChange}
        />
        {item.itemName}
      </label>
    </li>
  );
}
