import * as yup from 'yup'

// 身份证号码验证
const idNumberRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
// 手机号验证
const phoneRegex = /^1[3-9]\d{9}$/

// 公共验证规则
const common = {
  required: '此字段不能为空',
  select: '请选择一个选项',
  date: '请选择有效的日期',
  email: '请输入有效的邮箱地址',
  url: '请输入有效的网址',
  number: '请输入有效的数字',
  integer: '请输入整数',
  min: (min) => `不能小于${min}`,
  max: (max) => `不能大于${max}`,
  minLength: (min) => `至少需要${min}个字符`,
  maxLength: (max) => `不能超过${max}个字符`
}

// 验证规则
export const registrationSchemas = {
  // 运动员报名表单验证
  athlete: yup.object().shape({
    // 赛事信息
    eventId: yup.string().required(common.required),
    category: yup.string().required(common.required),
    
    // 个人信息
    realName: yup.string()
      .required(common.required)
      .min(2, common.minLength(2))
      .max(20, common.maxLength(20)),
    
    idNumber: yup.string()
      .required(common.required)
      .matches(idNumberRegex, '请输入有效的身份证号码'),
    
    phoneNumber: yup.string()
      .required(common.required)
      .matches(phoneRegex, '请输入有效的手机号码'),
    
    gender: yup.string().required(common.required),
    
    birthdate: yup.date()
      .required(common.required)
      .max(new Date(), '出生日期不能晚于今天')
      .typeError(common.date),
    
    height: yup.number()
      .required(common.required)
      .min(100, common.min(100))
      .max(250, common.max(250))
      .typeError(common.number),
    
    weight: yup.number()
      .required(common.required)
      .min(30, common.min(30))
      .max(200, common.max(200))
      .typeError(common.number),
    
    // 紧急联系人
    emergencyContact: yup.string()
      .required(common.required)
      .min(2, common.minLength(2))
      .max(20, common.maxLength(20)),
    
    emergencyPhone: yup.string()
      .required(common.required)
      .matches(phoneRegex, '请输入有效的手机号码'),
    
    emergencyRelation: yup.string()
      .required(common.required),
    
    // 运动经历
    experience: yup.string()
      .optional()
      .max(1000, common.maxLength(1000)),
    
    bestScore: yup.string()
      .optional()
      .max(100, common.maxLength(100)),
    
    // 健康状况
    healthCondition: yup.string()
      .optional()
      .max(500, common.maxLength(500)),
    
    medicalHistory: yup.string()
      .optional()
      .max(500, common.maxLength(500)),
    
    // 协议同意
    agreement: yup.boolean()
      .oneOf([true], '您必须同意协议才能提交报名')
  }),

  // 观众报名表单验证
  audience: yup.object().shape({
    eventId: yup.string().required(common.required),
    realName: yup.string()
      .required(common.required)
      .min(2, common.minLength(2))
      .max(20, common.maxLength(20)),
    
    idNumber: yup.string()
      .required(common.required)
      .matches(idNumberRegex, '请输入有效的身份证号码'),
    
    phoneNumber: yup.string()
      .required(common.required)
      .matches(phoneRegex, '请输入有效的手机号码'),
    
    ticketCount: yup.number()
      .required(common.required)
      .min(1, common.min(1))
      .max(5, common.max(5))
      .typeError(common.number),
    
    agreement: yup.boolean()
      .oneOf([true], '您必须同意协议才能提交报名')
  })
}

// 用户信息表单验证
export const userSchemas = {
  // 注册表单
  register: yup.object().shape({
    username: yup.string()
      .required(common.required)
      .min(4, common.minLength(4))
      .max(20, common.maxLength(20)),
    
    password: yup.string()
      .required(common.required)
      .min(6, common.minLength(6))
      .max(20, common.maxLength(20)),
    
    confirmPassword: yup.string()
      .required(common.required)
      .oneOf([yup.ref('password')], '两次输入的密码不一致'),
    
    email: yup.string()
      .required(common.required)
      .email(common.email),
    
    agreement: yup.boolean()
      .oneOf([true], '您必须同意用户协议才能注册')
  }),

  // 登录表单
  login: yup.object().shape({
    username: yup.string().required(common.required),
    password: yup.string().required(common.required),
    remember: yup.boolean()
  }),

  // 个人资料更新
  profile: yup.object().shape({
    nickname: yup.string()
      .optional()
      .max(20, common.maxLength(20)),
    
    realName: yup.string()
      .optional()
      .max(20, common.maxLength(20)),
    
    email: yup.string()
      .required(common.required)
      .email(common.email),
    
    phoneNumber: yup.string()
      .optional()
      .matches(phoneRegex, '请输入有效的手机号码'),
    
    avatar: yup.string().optional(),
    
    gender: yup.string().optional(),
    
    birthdate: yup.date()
      .optional()
      .max(new Date(), '出生日期不能晚于今天')
      .typeError(common.date)
  }),

  // 密码修改
  changePassword: yup.object().shape({
    oldPassword: yup.string().required(common.required),
    
    newPassword: yup.string()
      .required(common.required)
      .min(6, common.minLength(6))
      .max(20, common.maxLength(20)),
    
    confirmPassword: yup.string()
      .required(common.required)
      .oneOf([yup.ref('newPassword')], '两次输入的密码不一致')
  })
}

// 赛事表单验证
export const eventSchemas = {
  // 赛事创建/编辑表单
  event: yup.object().shape({
    name: yup.string()
      .required(common.required)
      .max(100, common.maxLength(100)),
    
    startTime: yup.date()
      .required(common.required)
      .typeError(common.date),
    
    endTime: yup.date()
      .required(common.required)
      .min(
        yup.ref('startTime'),
        '结束时间不能早于开始时间'
      )
      .typeError(common.date),
    
    registrationDeadline: yup.date()
      .required(common.required)
      .max(
        yup.ref('startTime'),
        '报名截止时间不能晚于开始时间'
      )
      .typeError(common.date),
    
    location: yup.string()
      .required(common.required)
      .max(200, common.maxLength(200)),
    
    maxParticipants: yup.number()
      .required(common.required)
      .min(1, common.min(1))
      .typeError(common.number),
    
    description: yup.string()
      .required(common.required)
      .max(2000, common.maxLength(2000)),
    
    rules: yup.string()
      .optional()
      .max(2000, common.maxLength(2000)),
    
    type: yup.string().required(common.required),
    
    status: yup.string().required(common.required),
    
    categories: yup.array()
      .of(
        yup.object().shape({
          name: yup.string().required(common.required),
          description: yup.string().optional()
        })
      )
      .min(1, '至少需要添加一个比赛项目')
  })
} 