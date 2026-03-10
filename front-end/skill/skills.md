---
inclusion: always
---

# BTP Module Creation Guide

## Rules
- **Service**: Static methods + `authApi` (NOT instance injection)
- **Hook**: SINGLE `useFeature` (NOT split hooks)
- **Modal**: Pure presentation, pass `FormEvent` to hook
- **Types**: `_id` (API) → `id` (View)
- **SCSS**: Use `@shared/styles/variables`
- **Exports**: `index.ts` in EVERY folder

## Structure
```
features/Feature/
├── index.ts
├── types/index.ts, IFeature.ts
├── services/index.ts, FeatureService.ts
├── hooks/index.ts, useFeaturePage.ts
├── components/index.ts, Table.tsx, Modal.tsx
└── views/index.ts, Page.tsx
```

## Types (`IFeature.ts`)
```ts
export interface IFeatureDto { _id: string; name: string; }
export interface IFeatureView { id: string; name: string; }
export interface IFeatureFormData { name: string; }
```

## Service (Static + authApi)
```ts
import { authApi, FEATURE_API_ENDPOINTS } from '@shared/api/authApi';
export class FeatureService {
  static async getList() { return authApi.get(FEATURE_API_ENDPOINTS.LIST); }
  static async create(data: IFormData) { return authApi.post(FEATURE_API_ENDPOINTS.CREATE, data); }
  static async update(id: string, data: IFormData) { return authApi.put(`${FEATURE_API_ENDPOINTS.UPDATE}/${id}`, data); }
  static async delete(id: string) { return authApi.delete(`${FEATURE_API_ENDPOINTS.DELETE}/${id}`); }
}
```

## Hook Pattern (Single Combined)
```ts
export const useFeature = () => {
  const [items, setItems] = useState<IView[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingItem, setEditingItem] = useState<IView | null>(null);
  const { success, error: showError, showConfirm } = useAlert();

  const transformToView = (dto: IDto): IView => ({ id: dto._id, name: dto.name });

  const fetchData = useCallback(async () => {
    setLoading(true);
    const res = await FeatureService.getList();
    setItems(res.data.items.map(transformToView));
    setLoading(false);
  }, []);

  const handleSubmit = useCallback(async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.currentTarget)) as IFormData;
    editingItem ? await FeatureService.update(editingItem.id, data) : await FeatureService.create(data);
    success('Saved'); setShowModal(false); fetchData();
  }, [editingItem, fetchData]);

  return { items, loading, showModal, editingItem, fetchData, handleSubmit, 
    handleEdit: (item: IView) => { setEditingItem(item); setShowModal(true); },
    handleAddNew: () => { setEditingItem(null); setShowModal(true); },
    handleClose: () => setShowModal(false) };
};
```

## Modal (Pure Presentation)
```tsx
export const FeatureModal: React.FC<{
  show: boolean; editingItem: IView | null; onClose: () => void;
  onSubmit: (e: FormEvent<HTMLFormElement>) => void;
}> = ({ show, editingItem, onClose, onSubmit }) => {
  if (!show) return null;
  return (
    <div className="modal-overlay">
      <form onSubmit={onSubmit}>
        <input name="name" defaultValue={editingItem?.name || ''} required />
        <button type="submit">Save</button>
      </form>
    </div>
  );
};
```

## Register Route
1. `constants.ts`: `ROUTES.FEATURE = "/feature"`
2. `IPermission.ts`: `PermissionModule.FEATURE = 'feature'`
3. `routeConfig.tsx`: Add `{ id, path, label, icon, permission, showInSidebar: true }`
4. `AppRoutes.tsx`: `<Route path={ROUTES.FEATURE} element={<ProtectedRoute requiredPermission={PermissionModule.FEATURE}><FeaturePage /></ProtectedRoute>} />`

## SCSS Variables
```scss
$primary-color, $color-success, $color-danger, $color-warning
$color-text, $color-border, $color-background
$spacing-xs/sm/md/lg/xl, $border-radius-sm/md/lg
```

## Aliases
`@app/*`, `@features/*`, `@shared/*`, `@assets/*`
