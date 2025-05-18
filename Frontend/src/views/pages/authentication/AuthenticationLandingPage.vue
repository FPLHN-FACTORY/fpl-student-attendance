<script setup>
import { useRoute, useRouter } from 'vue-router'
import imgLogoFpt from '@/assets/images/logo-fpt.png'
import imgLogoUdpm from '@/assets/images/logo-udpm.png'
import imgRoleAdmin from '@/assets/images/role-admin.png'
import imgRoleStaff from '@/assets/images/role-staff.png'
import imgRoleTeacher from '@/assets/images/role-teacher.png'
import imgRoleStudent from '@/assets/images/role-student.png'
import { GoogleOutlined } from '@ant-design/icons-vue'
import { onMounted, ref } from 'vue'
import requestAPI from '@/services/requestApiService'
import { REDIRECT_LOGIN_ADMIN } from '@/constants/authenticationConstant'
import useAuthStore from '@/stores/useAuthStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { decodeBase64 } from '@/utils/utils'
import { GLOBAL_ROUTE_NAMES, PREFIX_ADMIN_PANEL, URL_ADMIN_PANEL } from '@/constants/routesConstant'
import { ROUTE_NAMES_API } from '@/router/authenticationRoute'
import { ROLE } from '@/constants'
import { message } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()

const authStore = useAuthStore()
const loadingPage = useLoadingStore()

const roleLogin = ref(null)
const facilityID = ref(null)
const isShowModalSelectFacility = ref(false)
const lstFacility = ref([])

let roles = [
  {
    role: ROLE.STUDENT,
    label: 'Sinh viên',
    img: imgRoleStudent,
    route: GLOBAL_ROUTE_NAMES.STUDENT_PAGE,
  },
]

const isRouteAdm = route.path === PREFIX_ADMIN_PANEL
const isRoleAdm =
  authStore?.user?.role.includes(ROLE.ADMIN) ||
  authStore?.user?.role.includes(ROLE.STAFF) ||
  authStore?.user?.role.includes(ROLE.TEACHER)

if (isRouteAdm || isRoleAdm) {
  roles = [
    {
      role: ROLE.ADMIN,
      label: 'Cán bộ đào tạo',
      img: imgRoleAdmin,
      route: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
    },
    {
      role: ROLE.STAFF,
      label: 'Phụ trách xưởng',
      img: imgRoleStaff,
      route: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
    },
    {
      role: ROLE.TEACHER,
      label: 'Giảng viên',
      img: imgRoleTeacher,
      route: GLOBAL_ROUTE_NAMES.TEACHER_PAGE,
    },
  ]
}

const showModalSelectFacility = () => (isShowModalSelectFacility.value = true)

const handleSelectFacility = (role) => {
  roleLogin.value = role

  if (authStore.isLogin) {
    return redirectLoginRole()
  }

  if (role === ROLE.ADMIN) {
    return handleRedirectLogin(true)
  }
  showModalSelectFacility()
}

const handleRedirectLogin = (width_out_facility = false) => {
  message.destroy()
  if (!width_out_facility && !facilityID.value) {
    return message.error('Vui lòng chọn cơ sở muốn đăng nhập')
  }

  const currentRole = roles.find((o) => o.role.includes(roleLogin.value))

  if (!currentRole) {
    return message.error('Role đăng nhập không chính xác')
  }

  const params = new URLSearchParams({
    role: currentRole.role,
    redirect_uri: isRouteAdm ? URL_ADMIN_PANEL : window.location.origin,
    facility_id: facilityID.value,
  })

  window.location.href = `${REDIRECT_LOGIN_ADMIN}?${params}`
}

const fetchDataFacility = async () => {
  try {
    const response = await requestAPI.get(ROUTE_NAMES_API.FETCH_DATA_FACILITY)
    lstFacility.value = response.data.data
    if (lstFacility.value.length > 0) {
      facilityID.value = lstFacility.value[0].id
    }
  } catch (error) {
    message.error('Không thể tải danh sách cơ sở')
  }
}

const redirectLoginRole = () => {
  if (authStore.isLogin) {
    const role = roles.find((o) => o.role.includes(roleLogin.value))

    if (role) {
      loadingPage.hide()
      requestAPI
        .post(ROUTE_NAMES_API.FETCH_DATA_AVATAR, {
          url_image: authStore.user.picture,
        })
        .then(({ data: response }) => {
          authStore.updateUser({ picture: response.data })
        })
      return router.push({ name: role.route })
    }
  }
}

const checkLogin = () => {
  roleLogin.value = route.query.role || null
  const authenticationToken = route.query.authencation_token || null
  const authenticationError = route.query.authencation_error || null

  router.replace({ path: route.path, query: {} })

  loadingPage.show()
  if (authenticationToken) {
    if (!authStore.login(authenticationToken)) {
      loadingPage.hide()
      return message.error('Tài khoản của bạn không thể truy cập vào mục này!')
    }
  } else if (authenticationError) {
    const dataError = JSON.parse(decodeBase64(authenticationError))
    loadingPage.hide()
    return message.error(dataError.message)
  }
  redirectLoginRole()
}

onMounted(async () => {
  document.body.classList.add('bg-login')
  checkLogin()
  await fetchDataFacility()
  loadingPage.hide()
})
</script>

<template>
  <div class="container">
    <div class="row">
      <div class="logo">
        <img :src="imgLogoFpt" alt="Logo" />
        <img :src="imgLogoUdpm" alt="Logo" />
      </div>
    </div>

    <div class="row">
      <h2 class="title">Đăng nhập</h2>
      <div class="d-flex justify-content-center align-items-center">
        <div class="role-container">
          <template v-for="role in roles" :key="role.role">
            <div
              class="role-item"
              @click="handleSelectFacility(role.role)"
              v-if="
                !authStore.isLogin ||
                (authStore.isLogin && authStore.user.role.includes(role.role.toUpperCase()))
              "
            >
              <img :src="role.img" class="role-img" />
              <a-button class="role-button" size="large">
                <span>{{ role.label }}</span>
              </a-button>
            </div>
          </template>
        </div>
      </div>
      <p class="footer">Powered by <strong>FPLHN-UDPM</strong></p>
    </div>

    <a-modal v-model:open="isShowModalSelectFacility" centered :footer="null">
      <template #title>
        <div class="logo">
          <img :src="imgLogoFpt" alt="Logo" />
        </div>
      </template>

      <div class="d-flex justify-content-center flex-column">
        <a-select
          v-model:value="facilityID"
          placeholder="-- Chọn cơ sở đăng nhập -- "
          class="w-full"
        >
          <a-select-option v-for="o in lstFacility" :key="o.id" :value="o.id">
            {{ o.name }}
          </a-select-option>
        </a-select>

        <div class="kt-divider my-4">
          <span></span>
          <span>SOCIAL</span>
          <span></span>
        </div>

        <!-- Nút Google -->
        <a-button
          type="primary"
          danger
          class="bg-pink-500 text-white text-lg font-semibold flex items-center justify-center h-12 btn-google mb-3"
          size="large"
          :disabled="lstFacility.length < 1"
          @click="handleRedirectLogin()"
        >
          <GoogleOutlined class="mr-2" /> Google
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
  margin-top: 6rem;
}
.logo {
  text-align: center;
}
.btn-google {
  background-color: #ff5722;
}
.logo img {
  width: 200px;
  max-width: 100%;
  margin-bottom: 20px;
}
.title {
  margin: 2rem 0;
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #41395a;
  text-transform: uppercase;
}
.role-container {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 20px;
}
@media (max-width: 687px) {
  .role-container {
    flex-direction: column-reverse;
  }
}
.role-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}
.role-img {
  width: 280px;
  height: auto;
  max-width: 100%;
}
.role-button {
  width: 100%;
  margin-top: 10px;
  background-color: #41395b;
  border-color: #6b667d;
  color: white;
}
.role-button:hover,
.role-button:active {
  background-color: #6b667d;
  border-color: #6b667d;
  color: white;
}
.footer {
  margin-top: 6rem;
  font-size: 14px;
  color: gray;
  text-align: center;
}
.kt-divider {
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-pack: center;
  -ms-flex-pack: center;
  justify-content: center;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
}
.kt-divider > span:first-child {
  width: 100%;
  height: 1px;
  -webkit-box-flex: 1;
  -ms-flex: 1;
  flex: 1;
  background: #ebecf1;
  display: inline-block;
}
.kt-divider > span:last-child {
  width: 100%;
  height: 1px;
  -webkit-box-flex: 1;
  -ms-flex: 1;
  flex: 1;
  background: #ebecf1;
  display: inline-block;
}
.kt-divider > span:not(:first-child):not(:last-child) {
  padding: 0 2rem;
}
</style>
