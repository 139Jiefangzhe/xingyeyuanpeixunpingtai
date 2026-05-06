import { lazy } from "react";
import { RouteObject } from "react-router-dom";
import { system, user } from "../api";
import { getToken } from "../utils";
// 页面加载
import { InitPage } from "../pages/init";
import LoginPage from "../pages/login";
import WithFooter from "../pages/layouts/with-footer";
import WithoutFooter from "../pages/layouts/without-footer";

//用户中心页面
const MemberPage = lazy(() => import("../pages/member/index"));
//主页
const IndexPage = lazy(() => import("../pages/index/index"));
//修改密码页面
const ChangePasswordPage = lazy(() => import("../pages/change-password/index"));
//修改部门页面
const ChangeDepartmentPage = lazy(
  () => import("../pages/change-department/index")
);
//学习页面
const StudyPage = lazy(() => import("../pages/study/index"));
//课程页面
const CoursePage = lazy(() => import("../pages/course/index"));
const CoursePlayPage = lazy(() => import("../pages/course/video"));
//考试页面
const ExamListPage = lazy(() => import("../pages/exam/ExamList"));
const ExamRoomPage = lazy(() => import("../pages/exam/ExamRoom"));
const ExamResultPage = lazy(() => import("../pages/exam/ExamResult"));
//培训页面
const TrainTaskListPage = lazy(() => import("../pages/train/TrainTaskList"));

import PrivateRoute from "../components/private-route";

const LOCAL_DEV_BYPASS = import.meta.env.VITE_LOCAL_DEV_BYPASS === "true";
const LOCAL_USER_ID = Number(import.meta.env.VITE_LOCAL_USER_ID || "10005");

const localDevLoginData = {
  user: {
    id: LOCAL_USER_ID,
    name: "本地学员" + LOCAL_USER_ID,
    avatar: -1,
    email: "local-h5-" + LOCAL_USER_ID + "@playedu.test",
  },
  departments: [
    {
      id: 50,
      name: "市场部",
    },
  ],
  resource_url: {},
};

const localDevConfigData = {
  "ldap-enabled": "0",
  "system-h5-url": "",
  "system-logo": "",
  "system-name": "PlayEdu H5 Local",
  "system-pc-url": "",
  resource_url: {},
  "system-pc-index-footer-msg": "",
  "player-poster": "",
  "player-is-enabled-bullet-secret": "0",
  "player-disabled-drag": "0",
  "player-bullet-secret-text": "",
  "player-bullet-secret-color": "",
  "player-bullet-secret-opacity": "",
};

let RootPage: any = null;
if (LOCAL_DEV_BYPASS) {
  RootPage = (
    <InitPage configData={localDevConfigData} loginData={localDevLoginData} />
  );
} else if (getToken()) {
  RootPage = lazy(async () => {
    return new Promise<any>(async (resolve) => {
      try {
        let configRes: any = await system.config();
        let userRes: any = await user.detail();
        resolve({
          default: (
            <InitPage configData={configRes.data} loginData={userRes.data} />
          ),
        });
      } catch (e) {
        console.error("系统初始化失败", e);
      }
    });
  });
} else {
  RootPage = lazy(async () => {
    return new Promise<any>(async (resolve) => {
      try {
        let configRes: any = await system.config();
        resolve({
          default: <InitPage configData={configRes.data} />,
        });
      } catch (e) {
        console.error("系统初始化失败", e);
      }
    });
  });
}

const routes: RouteObject[] = [
  {
    path: "/",
    element: RootPage,
    children: [
      {
        path: "/",
        element: <WithFooter />,
        children: [
          {
            path: "/",
            element: <PrivateRoute Component={<IndexPage />} />,
          },
          {
            path: "/member",
            element: <PrivateRoute Component={<MemberPage />} />,
          },
          {
            path: "/study",
            element: <PrivateRoute Component={<StudyPage />} />,
          },
          {
            path: "/exam",
            element: <PrivateRoute Component={<ExamListPage />} />,
          },
          {
            path: "/train",
            element: <PrivateRoute Component={<TrainTaskListPage />} />,
          },
        ],
      },
      {
        path: "/",
        element: <WithoutFooter />,
        children: [
          {
            path: "/login",
            element: <LoginPage />,
          },
          {
            path: "/change-password",
            element: <PrivateRoute Component={<ChangePasswordPage />} />,
          },
          {
            path: "/change-department",
            element: <PrivateRoute Component={<ChangeDepartmentPage />} />,
          },
          {
            path: "/course/:courseId",
            element: <PrivateRoute Component={<CoursePage />} />,
          },
          {
            path: "/course/:courseId/hour/:hourId",
            element: <PrivateRoute Component={<CoursePlayPage />} />,
          },
          {
            path: "/exam/room/:paperId",
            element: <PrivateRoute Component={<ExamRoomPage />} />,
          },
          {
            path: "/exam/result/:paperId",
            element: <PrivateRoute Component={<ExamResultPage />} />,
          },
        ],
      },
    ],
  },
];

export default routes;
