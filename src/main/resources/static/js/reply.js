
async function get1(bno) {//함수 내부에서 await 사용 가능. 항상 Promise 반환
    const result = await axios.get(`/replies/list/${bno}`) //HTTP GET 요청 보내는 함수
                // 서버 응답이 올 때까지 기다림. 비동기 호출
    // console.log(result)
    // return result.data;
    return result
}
async function getList({bno, page, size, goLast}) {
    const result = await axios.get(`/replies/list/${bno}`, {params: {page, size}})
    if(goLast){
        const total = result.data.total
        const lastPage = parseInt(Math.ceil(total/size))
        return getList({bno:bno, page:lastPage, size:size})
    }
    return result.data
}
async function addReply(replyObj) {
    const response = await axios.post(`/replies/`,replyObj)
    return response.data
}
async function getReply(rno) {
    const response = await axios.get(`/replies/${rno}`)
    return response.data
}
async function modifyReply(replyObj) {
    const response = await axios.put(`/replies/${replyObj.rno}`, replyObj)
    return response.data
}
async function removeReply(rno) {
    const response = await axios.delete(`/replies/${rno}`)
    return response.data
}
















