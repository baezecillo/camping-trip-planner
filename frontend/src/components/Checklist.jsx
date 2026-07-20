import ChecklistItem from './ChecklistItem';

export default function Checklist({ items, onItemUpdated }) {
  const categories = [];
  const itemsByCategory = new Map();
  for (const item of items) {
    if (!itemsByCategory.has(item.category)) {
      itemsByCategory.set(item.category, []);
      categories.push(item.category);
    }
    itemsByCategory.get(item.category).push(item);
  }

  return (
    <div className="card">
      <h2>Checklist</h2>
      {categories.map((category) => (
        <section key={category} className="checklist-category">
          <h3>{category}</h3>
          <ul className="checklist-list">
            {itemsByCategory.get(category).map((item) => (
              <ChecklistItem
                key={item.id}
                item={item}
                onItemUpdated={onItemUpdated}
              />
            ))}
          </ul>
        </section>
      ))}
    </div>
  );
}
