export enum CategoryStatus {
  Draft = 1,
  PendingApproval = 3,
  Approved = 4,
  Rejected = 5,
  CancelApproved = 7,
}

export function getCategoryStatusLabel(status: number | null | undefined): string {
  switch (status) {
    case CategoryStatus.Draft:
      return 'Tạo mới';
    case CategoryStatus.PendingApproval:
      return 'Chờ phê duyệt';
    case CategoryStatus.Approved:
      return 'Đã phê duyệt';
    case CategoryStatus.Rejected:
      return 'Từ chối';
    case CategoryStatus.CancelApproved:
      return 'Hủy duyệt';
    default:
      return 'Không xác định';
  }
}

export function getCategoryStatusClass(status: number | null | undefined): string {
  switch (status) {
    case CategoryStatus.Draft:
      return 'bg-cyan-50 text-cyan-700';
    case CategoryStatus.PendingApproval:
      return 'bg-amber-50 text-amber-700';
    case CategoryStatus.Approved:
      return 'bg-green-50 text-green-700';
    case CategoryStatus.Rejected:
      return 'bg-red-50 text-red-700';
    case CategoryStatus.CancelApproved:
      return 'bg-slate-100 text-slate-700';
    default:
      return 'bg-slate-100 text-slate-700';
  }
}
