function loginApi(data) {
  return $axios({
    'url': '/employee/login',
    // 'url': 'http://localhost:8080/employee/login',
    'method': 'post',
    data
  })
}

function logoutApi(){
  return $axios({
    'url': '/employee/logout',
    'method': 'post',
  })
}
