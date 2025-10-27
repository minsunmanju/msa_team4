export function ddayStr(deadline) {
  if (!deadline) return null;
  const today = new Date(); today.setHours(0,0,0,0);
  const end = new Date(deadline);
  const diff = Math.ceil((end.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  if (diff > 0) return `D-${diff}`;
  if (diff === 0) return "D-DAY";
  return `D+${Math.abs(diff)}`;
}

export function isClosedUI(isClosed, deadline) {
  if (isClosed) return true;
  if (!deadline) return false;
  const today = new Date(); today.setHours(0,0,0,0);
  return new Date(deadline) < today;
}
