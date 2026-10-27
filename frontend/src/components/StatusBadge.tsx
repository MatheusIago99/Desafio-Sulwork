import type { StatusEvento } from '../api/types';
import { STATUS_LABEL } from '../utils/format';

export function StatusBadge({ status }: { status: StatusEvento }) {
  return <span className={`badge badge-status-${status.toLowerCase()}`}>{STATUS_LABEL[status]}</span>;
}
