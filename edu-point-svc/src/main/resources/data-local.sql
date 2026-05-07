INSERT INTO point_rule (id, name, rule_type, points, description, status, is_del) VALUES
  ('rule-local-001', '考试通过', 'EXAM_PASS', 100, '通过一场考试获得100积分', 1, 0),
  ('rule-local-002', '课程学完', 'COURSE_COMPLETE', 50, '完成一门课程学习获得50积分', 1, 0),
  ('rule-local-003', '培训结业', 'TRAIN_COMPLETE', 200, '完成整个培训项目获得200积分', 1, 0),
  ('rule-local-004', '每日签到', 'LOGIN_DAILY', 10, '每日登录获得10积分', 1, 0);

INSERT INTO point_product (id, name, description, image_url, points_price, stock, status, sort, is_del) VALUES
  ('prod-local-001', '企业定制笔记本', '高品质商务笔记本，印有公司LOGO', 'https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=960&q=80', 500, 100, 1, 1, 0),
  ('prod-local-002', '咖啡券', '楼下咖啡厅免费兑换券', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=960&q=80', 200, 500, 1, 2, 0),
  ('prod-local-003', '学习书籍《Effective Java》', '技术类经典书籍', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=960&q=80', 800, 50, 1, 3, 0),
  ('prod-local-004', '半天调休券', '可用于兑换半天带薪调休', 'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=960&q=80', 2000, 20, 1, 4, 0);

INSERT INTO point_order (id, user_id, product_id, product_name, points_price, quantity, total_points, status, address, remark) VALUES
  ('order-local-001', 10005, 'prod-local-002', '咖啡券', 200, 1, 200, 'PENDING', '上海市静安区企业培训中心', '待发货样例订单'),
  ('order-local-002', 10006, 'prod-local-001', '企业定制笔记本', 500, 1, 500, 'SHIPPED', '北京市朝阳区望京SOHO', '已发货样例订单');
