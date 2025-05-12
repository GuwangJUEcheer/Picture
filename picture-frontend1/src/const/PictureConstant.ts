interface Options{
  label:string,
  value:string,
}
const PictureFormatOptions: Options[] = [
  {
    label: '',
    value: '',
  },
  {
    label: 'jpg',
    value: 'jpg',
  },
  {
    label: 'png',
    value: 'png',
  },
  {
    label: 'jpeg',
    value: 'jpeg',
  },
]

export {
  PictureFormatOptions,
  type Options,
}

