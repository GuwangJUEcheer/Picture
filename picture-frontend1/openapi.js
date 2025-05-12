import {generateService} from '@umijs/openapi'
generateService({
  requestLibPath:"import index from '@/index'",
  schemaPath: 'http://localhost:8100/api/v2/api-docs',
  serversPath: './src',
})
