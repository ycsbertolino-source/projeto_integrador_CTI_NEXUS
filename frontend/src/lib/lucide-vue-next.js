import { h } from 'vue'

const makeIcon = (paths) => (props, { attrs }) => {
  const classes = attrs.class || ''
  return h('svg', { xmlns: 'http://www.w3.org/2000/svg', viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.5', strokeLinecap: 'round', strokeLinejoin: 'round', class: classes }, paths.map(d => h('path', { d })))
}

export const FileSpreadsheet = makeIcon([
  'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z',
  'M14 2v6h6',
  'M3 13h8',
  'M3 17h8',
  'M3 9h8'
])

export const CheckCircle2 = makeIcon([
  'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20z',
  'M9 12l2 2 4-4'
])

export const AlertTriangle = makeIcon([
  'M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94A2 2 0 0 0 22.18 18L13.71 3.86a2 2 0 0 0-3.42 0z',
  'M12 9v4',
  'M12 17h.01'
])

export const ListTree = makeIcon([
  'M8 6h13',
  'M8 12h13',
  'M8 18h13',
  'M3 6h.01',
  'M3 12h.01',
  'M3 18h.01'
])

export const UploadCloud = makeIcon([
  'M21 15v2a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-2',
  'M7 10l5-5 5 5',
  'M12 5v12'
])

export const X = makeIcon([
  'M18 6L6 18',
  'M6 6l12 12'
])

export const LayoutGrid = makeIcon([
  'M3 3h7v7H3z',
  'M14 3h7v7h-7z',
  'M3 14h7v7H3z',
  'M14 14h7v7h-7z'
])

export const ArrowLeft = makeIcon([
  'M19 12H5',
  'M12 19l-7-7 7-7'
])

export const Loader2 = (props, { attrs }) => h('svg', { xmlns: 'http://www.w3.org/2000/svg', viewBox: '0 0 24 24', class: attrs.class }, [h('circle', { cx: 12, cy: 12, r: 10, stroke: 'currentColor', 'stroke-width': '4', fill: 'none', 'stroke-opacity': 0.25 }), h('path', { d: 'M22 12a10 10 0 0 0-10-10', stroke: 'currentColor', 'stroke-width': '4', 'stroke-linecap': 'round' })])

export default {}
