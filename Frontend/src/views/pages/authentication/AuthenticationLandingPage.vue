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
import { toast } from 'vue3-toastify'
import requestAPI from '@/services/requestApiService'
import { REDIRECT_LOGIN_ADMIN } from '@/constants/authenticationConstant'
import useAuthStore from '@/stores/useAuthStore'
import useLoadingStore from '@/stores/useLoadingStore'
import { decodeBase64 } from '@/utils/utils'
import { GLOBAL_ROUTE_NAMES } from '@/constants/routesConstant'
import { ROUTE_NAMES_API } from '@/router/authenticationRoute'
import { ROLE } from '@/constants/roleConstant'

const router = useRouter()
const route = useRoute()

const authStore = useAuthStore()
const loadingPage = useLoadingStore()

const roleLogin = ref(null)
const facilityID = ref(null)
const isShowModalSelectFacility = ref(false)
const lstFacility = ref([])

const roles = [
  {
    role: ROLE.ADMIN,
    label: 'Cán bộ đào tạo',
    img: imgRoleAdmin,
    route: GLOBAL_ROUTE_NAMES.ADMIN_PAGE,
  },
  {
    role: ROLE.STAFF,
    label: 'Quản lý xưởng',
    img: imgRoleStaff,
    route: GLOBAL_ROUTE_NAMES.STAFF_PAGE,
  },
  {
    role: ROLE.TEACHER,
    label: 'Giảng viên',
    img: imgRoleTeacher,
    route: GLOBAL_ROUTE_NAMES.TEACHER_PAGE,
  },
  {
    role: ROLE.STUDENT,
    label: 'Sinh viên',
    img: imgRoleStudent,
    route: GLOBAL_ROUTE_NAMES.STUDENT_PAGE,
  },
]

const showModalSelectFacility = () => (isShowModalSelectFacility.value = true)

const handleSelectFacility = (role) => {
  roleLogin.value = role
  if (role === ROLE.ADMIN) {
    return handleRedirectLogin(true)
  }
  showModalSelectFacility()
}

const handleRedirectLogin = (width_out_facility = false) => {
  toast.clearAll()
  if (!width_out_facility && !facilityID.value) {
    return toast.error('Vui lòng chọn cơ sở muốn đăng nhập')
  }

  const currentRole = roles.find((o) => o.role.includes(roleLogin.value))

  if (!currentRole) {
    return toast.error('Role đăng nhập không chính xác')
  }

  const params = new URLSearchParams({
    role: currentRole.role,
    redirect_uri: window.location.origin,
    facility_id: facilityID.value,
  })

  window.location.href = `${REDIRECT_LOGIN_ADMIN}?${params}`
}

const fetchDataFacility = async () => {
  try {
    const response = await requestAPI.get(ROUTE_NAMES_API.FETCH_DATA_FACILITY)
    lstFacility.value = response.data.data
  } catch (error) {
    toast.error('Không thể tải danh sách cơ sở')
  }
}

const redirectLoginRole = () => {
  if (authStore.isLogin) {
    const role = roles.find((o) => o.role.includes(roleLogin.value))

    if (role) {
      loadingPage.hide()
      return router.push({ name: role.route })
    }
  }
}

const checkLogin = () => {
  roleLogin.value = route.query.role || null
  const authenticationToken = route.query.authencation_token || null
  const authenticationError = route.query.authencation_error || null

  loadingPage.show()

  if (authenticationToken) {
    if (!authStore.login(authenticationToken)) {
      loadingPage.hide()
      return toast.error('Tài khoản của bạn không thể truy cập vào mục này!')
    }
  } else if (authenticationError) {
    const dataError = JSON.parse(decodeBase64(authenticationError))
    loadingPage.hide()
    return toast.error(dataError.message)
  }
  redirectLoginRole()
}

onMounted(() => {
  document.body.classList.add('bg-login')
  sessionStorage.setItem('selectedKeys', JSON.stringify(['1']))
  checkLogin()
  fetchDataFacility()
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
          <div
            v-for="role in roles"
            :key="role.role"
            class="role-item"
            @click="handleSelectFacility(role.role)"
          >
            <img :src="role.img" class="role-img" />
            <a-button class="role-button" size="large">
              <span>{{ role.label }}</span>
            </a-button>
          </div>
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
          class="bg-pink-500 text-white text-lg font-semibold flex items-center justify-center h-12"
          size="large"
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
