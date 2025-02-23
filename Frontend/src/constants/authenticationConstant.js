import { BASE_URL, API_URL } from './routesConstant'

export const AUTHENCATION_STORAGE_TOKEN = 'i_n_u_ha'

export const AUTHENCATION_STORAGE_USER_DATA = 'i_n_u_h_a'

export const REDIRECT_LOGIN_ADMIN = BASE_URL + '/authorization'

export const API_ROUTES = {
  FETCH_DATA_FACILITY: API_URL + '/get-all-facility',
  FETCH_INFO_USER: API_URL + '/get-info-user',
}

export const ROUTE_NAMES = {
  LOGIN_PAGE: 'at_login_page',
  LOGOUT_PAGE: 'at_logout_page',
}
