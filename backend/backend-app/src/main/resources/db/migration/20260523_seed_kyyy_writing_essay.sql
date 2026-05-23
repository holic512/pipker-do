-- @file kyyy_writing_essay_seed
-- @project pipker-do
-- @module 考研英语 / 作文知识库
-- @description 将英语考研作文 Markdown 内容转换为作文知识库初始化数据。
-- @logic 1. 预置英一英二 2010-2025 年大小作文；2. 保留题目、范文及中英翻译；3. 使用 writing_code 做幂等更新。
-- @dependencies Table: kyyy_writing_essay, Script: backend/backend-kyyy/scripts/generate_writing_sql_from_markdown.py
-- @index_tags 考研英语, 作文, 初始化数据, SQL, 知识库
-- @author holic512
SET NAMES utf8mb4;


INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2010_small',
    'english_one',
    2010,
    'small',
    'notice',
    '2010年英一小作文',
    10,
    100,
    100,
    'Part A
51. Directions:
You are supposed to write for the Postgraduates’ Associ ation a notice to recruit volunteers for an international c onference on globalization. The notice should include the basic qualificationsof applicants and other information which you think is relevant.
You should write about 100 words on ANSWER SHEET 2.
Do not sign your own nam e at the end of the notice. Use "postgraduates’ Association" instead. （10 points）',
    'Notice
Volunteers are needed for an international conference on globalization.
Applicants should have a good command of English, strong communication skills and a serious sense of responsibility. Those with experience in organizing activities or receiving foreign guests will be preferred. The main duties include helping with reception, providing basic language support and assisting the conference staff during the event.
If you are interested, please contact the Postgraduates'' Association before March 1.
Postgraduates'' Association',
    '你是研究生会成员，请写一则通知，为一场关于全球化的国际会议招募志愿者。通知中应包括报名者的基本条件以及你认为相关的其他信息。请在答题卡2上写约100词。结尾不要署自己的名字，用“Postgraduates’ Association”代替。（10分）',
    '现招募国际全球化会议志愿者。凡英语基础扎实、具有跨文化沟通经验的同学，均可报名参加。该岗位的主要任务包括：第一，向来自不同国家和地区的来宾介绍会议主题；第二，为与会代表提供中英文口译服务，以保证会议顺利进行；第三，按照会议日程准时到岗并完成相关工作安排。欢迎对本活动感兴趣的同学踊跃报名。请于2010年3月1日前拨打010-12345678与我们联系。
研究生会
2010年1月9日',
    '英一,小作文,notice,历年真题,范文',
    '有关文档/英语考研作文/英一/2010.md',
    1,
    20101
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2010_big',
    'english_one',
    2010,
    'big',
    'picture',
    '2010年英一大作文',
    20,
    160,
    200,
    'Part B
52. Directions:
Write an essay of 160-200 words based on the following drawing. In your essay, you should
1) describe the drawing briefly,
2) explain its intended meaning, and
3) give your comments.
You should write neatly on ANSWER SHEET 2. （20 points）',
    'The drawing presents a steaming hot pot filled with ingredients labelled with both Chinese and foreign cultural elements such as literature, moral values and performing arts. The image suggests that cultural diversity, when brought together, can create richness rather than conflict.
Its message is clear. In today''s China, domestic culture and foreign culture are not simply confronting each other; they are also learning from and blending with one another. With the country becoming more open to the world, foreign visitors are increasingly interested in Chinese traditions, while Chinese people are also exposed to ideas and practices from abroad. This exchange promotes understanding and mutual respect.
In my view, cultural interaction is an inevitable result of globalization. What matters is that we stay confident in our own traditions while remaining open-minded enough to appreciate and learn from others.',
    '请根据所给图画写一篇160-200词的短文。写作时你应当：1）简要描述图画；2）解释其寓意；3）发表你的评论。请工整地写在答题卡2上。（20分）',
    '图画中，一口热气腾腾的火锅里汇聚了文学、道德观念、表演艺术等中外文化元素，正因为各种文化交融在一起，这口“文化火锅”才显得格外丰富而美味。
这幅图反映了当代中国社会中中西文化相互碰撞又彼此融合的现实。随着中国不断扩大对外开放，越来越多外国人被中国文化所吸引，并逐渐理解和喜爱它。同时，中国人也在与世界交流的过程中接触到更多外来文化。正是在这种双向互动中，不同民族之间得以增进了解，和平共处。
在我看来，任何民族文化都是珍贵遗产，也属于全人类。经济全球化背景下，不同文化的交流与融合已成为不可逆转的时代趋势。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2010.md',
    1,
    20102
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2011_small',
    'english_one',
    2011,
    'small',
    'letter',
    '2011年英一小作文',
    10,
    100,
    100,
    'Part A
51. Directions:
Write a letter to a friend of yours to
1) recommend one of your favorite movies and
2) give reasons for your recommendation.
You should write about 100 words on ANSWER SHEET 2.
Do not sign your own name at the end of the letter. Use “Li Ming” instead.
Do not write the address. (10 points)',
    'Dear John,
I am writing, without hesitation, to share one of my favorite movies, Forest Gump, with you, which is not only conducive to your study, but also beneficial to your life.
For one thing, the beautiful language in this original English movie may contribute to your study of English in listening, speaking, reading and writing. For another thing, the profound cultural elements implicit in the scene will equip you with foreign cultural background and, above all, enrich your daily life.
Would you like to see this movie after my recommendation? Remember to tell me your opinion about the movie. I am looking forward to your early reply.
Yours,
Li Ming',
    '你的朋友想找一份兼职工作，请给他写一封信：1）推荐一部你最喜欢的电影；2）说明理由。请在答题卡2上写约100词。不要在信末署自己的真实姓名，用“Li Ming”代替。不要写地址。（10分）',
    '亲爱的 John：
我写信是想毫不犹豫地向你推荐我最喜欢的一部电影《阿甘正传》。
之所以推荐这部电影，首先是因为它情节感人、人物鲜明，能够给人带来强烈的情感共鸣。更重要的是，影片通过阿甘的人生经历告诉我们：只要坚持、真诚并勇敢面对生活中的困难，每个人都可能实现自己的价值。它不仅是一部优秀的电影，也是一种积极向上的人生启示。
如果你有时间，我真心希望你能去看一看。
你真诚的，
Li Ming',
    '英一,小作文,letter,历年真题,范文',
    '有关文档/英语考研作文/英一/2011.md',
    1,
    20111
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2011_big',
    'english_one',
    2011,
    'big',
    'picture',
    '2011年英一大作文',
    20,
    160,
    200,
    'Part B
52. Directions:
Write an essay of 160-200 words based on the following drawing. In your essay, you should
1) describe the drawing briefly,
2) explain its intended meaning, and
3) give your comments.
You should write neatly on ANSWER SHEET 2. (20 points)',
    'The terrible scene depicted in the cartoon shows that some people in our life still lack the awareness of environmental protection. The picture illustrates that two tourists are chatting and eating happily on a boat and casually throwing their rubbish into the lake which is full of litter and waste. The drawing sets us thinking too much due to its far-reaching influence.
Nowadays, though the awareness of protecting environment is being accepted by more and more people, we can still see many unpleasant scenes especially in scenic spots. Why does this phenomenon arise? Many factors are accounting for it. First and foremost, to some people, the consciousness of protecting environment is still not so strong. They may not think it is a big deal to throw rubbish everywhere. In addition, the environmental management system isn’t so satisfying. For example, in some places there’re few regulations or the implementation is seldom performed actually.
From what has been discussed above, it is urgent to take some effective and relative measures. In the first place, we should continue to conduct more propaganda in communities and schools so as to let people realize the importance of protecting environment. In the second, more rules should be made and carried out by the government to restrain the conduction of destroying environment. People should work together to create clean and beautiful surroundings.',
    '请根据所给图画写一篇160-200词的短文。写作时你应当：1）简要描述图画；2）解释其寓意；3）发表你的评论。请工整地写在答题卡2上。（20分）',
    '漫画展示了两名游客一边在船上聊天、吃东西，一边把垃圾随手扔进已经满是废弃物的湖中。这一场景说明，在现实生活中仍有不少人缺乏环境保护意识。
如今，虽然越来越多人开始认同环保的重要性，但在景区等公共场所，类似的不文明现象仍然时有发生。原因之一是部分人环保意识薄弱，认为随手丢垃圾并不严重；另一个原因是相关管理制度还不够完善，缺乏有效约束与执行。
因此，我们有必要采取更有力的措施。一方面，要继续通过社区、学校和媒体加强环保宣传；另一方面，也应完善规章制度并严格执行。只有全社会共同努力，才能创造更加整洁优美的环境。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2011.md',
    1,
    20112
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2012_small',
    'english_one',
    2012,
    'small',
    'email',
    '2012年英一小作文',
    10,
    100,
    100,
    '51. Directions:
Some international students are coming to your university. Write them an email in the name of the Students’ Union to
1) extend your welcome and
2) provide some suggestions for their campus life here.
You should write about 100 words on ANSWER SHEET 2.
Do not sign your own name at the end of the letter. Use “Li Ming” instead.
Do not write the address. (10 points)',
    'Dear All,
I am writing on behalf of our Student’s Association to send our warm welcome. And in order to make you adjust life in China, I am making some constructive advices with regard to the life in our university.
To begin with, you’d better grasp the basic communicating vocabulary as much as possible so as to freely express yourself. In addition, you can read some books on Chinese customs and daily life style in case you feel uneasy once join a completely strange context. Finally, relax yourself and feel confident toward your future life.
I hope you will find these proposals useful, and I would be ready to discuss this matter with you to further details.
Sincerely yours,
Li Ming',
    '学校将举办一场国际会议，请你代表学生会给与会者写一封欢迎信。请在答题卡2上写约100词。不要在信末署自己的真实姓名，用“Li Ming”代替。（10分）',
    '亲爱的各位来宾：
我谨代表学生会向你们表示热烈欢迎，并由衷感谢各位参加本次国际会议。
这次会议为不同国家和地区的参会者提供了面对面交流的平台，也为我们学习新思想、了解不同文化创造了宝贵机会。我们真诚希望各位在会议期间积极讨论、分享见解，并在校园里度过一段愉快而充实的时光。
预祝会议圆满成功，也祝各位在中国一切顺利。
你真诚的，
Li Ming',
    '英一,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英一/2012.md',
    1,
    20121
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2012_big',
    'english_one',
    2012,
    'big',
    'picture',
    '2012年英一大作文',
    20,
    160,
    200,
    'Part B
52. Directions:
Write an essay of 160-200 words based on the following drawing. In your essay, you should
1) describe the drawing briefly,
2) explain its intended meaning, and
3) give your comments.
You should write neatly on ANSWER SHEET 2. (20 points)
### 解析/补充说明
52.大作文',
    'The cartoon shows a bottle that has fallen over, with some water already spilled out. One man looks at it sadly and says that nothing is left, while the other picks it up and says happily that there is still some water remaining. The same situation leads to two completely different reactions.
The message is that perspective shapes attitude. If we view problems pessimistically, we tend to magnify loss and become discouraged. If we stay optimistic, however, we are more likely to notice possibilities and look for solutions. In real life, setbacks are unavoidable, but our response to them often determines whether we give up or move forward.
Therefore, it is wise to cultivate a positive mindset. Seeing the brighter side of things does not mean denying difficulty; it means facing difficulty with confidence and hope.',
    '请根据所给图画写一篇160-200词的短文。写作时你应当：1）简要描述图画；2）解释其寓意；3）发表你的评论。请工整地写在答题卡2上。（20分）',
    '图画中，一个倒下的瓶子里还剩下一点水。一个人沮丧地说“什么都没剩下”，另一个人却高兴地说“太幸运了，还剩一点”。他们面对同一情境，却表现出完全不同的态度。
这幅图说明，看待问题的角度不同，得出的感受与结果也会截然不同。消极的人往往只看到损失与困难，而积极的人则更容易发现希望与补救的空间。现实生活中，挫折和不如意不可避免，但如果能够调整心态、换个角度思考，就更可能找到解决办法。
因此，当我们遭遇困难时，更应该保持乐观、理性和积极的态度。这样的人生态度，往往也是走出困境、获得幸福的重要前提。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2012.md',
    1,
    20122
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2013_small',
    'english_one',
    2013,
    'small',
    'practical_writing',
    '2013年英一小作文',
    10,
    100,
    100,
    'Part A
51. Directions:
Write an e-mail of about 100 words to a foreign teacher in yourcollege, inviting him/her to be a judge for the upcoming English speech contest.
You should include the details you think necessary.
You should write neatly on the ANSWER SHEET 2.
Do not sign your own name at the end of the e-mail. Use “Li Ming” instead.
Do not write the address. (10 points)',
    'Dear Professor Smith,
On behalf of the Students'' Union, I am writing to invite you to serve as a judge for the English speech contest to be held in the Students'' Union Hall next Monday.
As a highly respected scholar in English language and culture, you are in an ideal position to offer professional and valuable comments to the contestants. Your presence would not only ensure the quality of the competition but also greatly encourage the students.
We would be honored if you could accept our invitation. I am looking forward to your reply.
Yours sincerely,
Li Ming',
    '请给你所在学校的一位外教写一封约100词的电子邮件，邀请他/她担任即将举行的英语演讲比赛评委。内容可包括你认为必要的细节。请工整地写在答题卡2上。结尾不要署自己的名字，用“Li Ming”代替。不要写地址。（10分）',
    '亲爱的 Smith 教授：
我谨代表学生会写信邀请您担任本周一在学生会礼堂举行的英语演讲比赛评委。
您在英语语言与文化方面造诣深厚、深受师生欢迎，我们相信，如果您能莅临现场，不仅会为比赛增添光彩，也一定能对同学们的口语和写作能力提升提出宝贵建议。
如果您能接受邀请，我们将不胜荣幸。期待您的回复。
你真诚的，
Li Ming',
    '英一,小作文,practical_writing,历年真题,范文',
    '有关文档/英语考研作文/英一/2013.md',
    1,
    20131
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2013_big',
    'english_one',
    2013,
    'big',
    'picture',
    '2013年英一大作文',
    20,
    160,
    200,
    'Part B
52. Directions:
Write an essay of 160-200 words based on the following drawing. In your essay you should
1) describe the drawing briefly
2) explain its intended meaning, and
3) give your comments
You should write neatly on the ANSWER SHEET 2. (20 points)',
    'The drawing presents several possible paths that graduate students may choose after graduation: finding a job, pursuing further study, starting a business or going abroad. It reminds us that major life decisions are both common and unavoidable.
Its implication is that choices are closely connected with a person''s future happiness and development. For university students, graduation is a turning point. Some may be better suited to academic study, while others may prefer practical work experience or even entrepreneurial attempts. Since everyone has different personalities, goals and family backgrounds, the right choice can only be made after careful self-assessment and realistic judgment.
In my opinion, making a wise decision requires both confidence and self-awareness. More importantly, once a decision is made, one should stick to it and work hard for the chosen goal.',
    '请根据所给图画写一篇160-200词的短文。写作时你应当：1）简要描述图画；2）解释其寓意；3）发表你的评论。请工整地写在答题卡2上。（20分）',
    '图画描绘了研究生毕业时常见的选择：找工作、继续深造、创业或出国。面对这些道路，每个人都必须作出取舍。
这幅图告诉我们，人生中的重要选择往往与幸福感和发展路径密切相关。对大学生而言，毕业是一个关键转折点。有人更适合继续学习，有人更适合尽早步入职场，也有人希望尝试创业。正确的选择并不存在统一答案，它取决于个人能力、兴趣、家庭情况以及对现实的判断。
在我看来，作出选择时既要清醒认识自己，也要保持务实态度。一旦决定了方向，就应认真投入并坚持走下去。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2013.md',
    1,
    20132
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2014_small',
    'english_one',
    2014,
    'small',
    'letter',
    '2014年英一小作文',
    10,
    100,
    100,
    'Part A
51. Directions:
Write a letter of about 100 words to the president of your university, suggesting how to improve students’ physical condition.
You should include the details you think necessary.
You should write neatly on the ANSWER SHEET.
Do not sign your own name at the end of the letter. Use “Li Ming” instead.
Do not write the address.(10 points)',
    'Dear Mr. President,
It is my great honor to write to you. As far as I am concerned, we have enough extraordinary lectures and what we need now is physical exercise.
Since most of the time is spent in watching TV and playing computer games, our physical conditions are not good enough. i still have some suggestions for you. To begin with, our university should arrange more PE classes. To continue, we should have a fixed schedule for a certain time of outdoor activity. What is more, teaching faculty should be involved in the same kind of physical exercise.
I hope that our university could take the responsibility for our students’ physical health. I will be highly grateful if you could take my suggestions into account.
Yours sincerely,
Li Ming',
    '假设你即将出国学习，并将与当地学生 John 合租公寓。请给他写一封邮件：1）介绍你的生活习惯；2）请他就当地生活给你一些建议。请在答题卡上写约100词。不要署自己的名字，用“Li Ming”代替。不要写地址。（10分）',
    '亲爱的 John：
我叫 Li Ming，即将到你所在的大学学习，并将与你合住。我写这封邮件，是想先向你介绍一下自己的生活习惯，也希望你能给我一些适应当地生活的建议。
我通常早上六点起床，喜欢做些运动；闲暇时间则更愿意去图书馆阅读。另外，我比较注重整洁，也希望居住环境安静有序。与此同时，我也想请你介绍一下当地生活中需要特别注意的事情，好让我提前做好准备。
期待尽快见到你，也祝一切顺利。
你真诚的，
Li Ming',
    '英一,小作文,letter,历年真题,范文',
    '有关文档/英语考研作文/英一/2014.md',
    1,
    20141
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2014_big',
    'english_one',
    2014,
    'big',
    'picture',
    '2014年英一大作文',
    20,
    160,
    200,
    'Part B
52. Directions:
Write an essay of 160-200 words based on the following drawing. In your essay, you should
1）describe the drawing briefly,
2）interpret its intended meaning, and
3）give your comments.
You should write neatly on the ANSWER SHEET (20 points)',
    'As is vividly described in the left part of the drawing, thirty years ago, there stood a delicate mother, holding the tiny hand of a lovely girl, who wore a red scarf. On the contrary, the right part of the picture illustrates that with time flying quickly, the little girl, who has already grown up as a gorgeous lady, is supporting her old mother. We are informed: accompanying.
It is without saying that the old and the young are two indispensable parts in society. On the one hand, what we have and enjoy now was created by our parents in the early days, as the old Chinese saying goes, "One generation plants tress under whose shade another generation rests". On the other hand, all of us are supposed to take good care of the youngsters, too. It is children who make us see the future of our state, for they are the future builders of our country.
The young should consider it a moral obligation respecting and taking care of old parents. Meanwhile, it is also the duty of the parents to protect, educate and look after the youths. Let''s bear this in mind and cultivate that virtue together, because only by doing so, can we feel as if we were living in a happy and harmonious family.',
    '请根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请在答题卡上写约150词。（15分）',
    '图表展示了1990年、2000年和2010年中国城乡人口数量的变化：城市人口持续增长，而农村人口则逐步减少。
这一趋势与我国快速推进的城市化密切相关。首先，城市能提供更多就业机会、更完善的公共服务以及更加便利的生活条件，因此吸引了大量人口流入。其次，部分农村地区经济发展相对缓慢，年轻人更倾向于进入城市寻求更好的发展空间。
总体来看，人口向城市集中是现代化进程中的普遍现象。但在这一过程中，也应同步加强农村建设，推动城乡协调发展。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2014.md',
    1,
    20142
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2015_small',
    'english_one',
    2015,
    'small',
    'notice',
    '2015年英一小作文',
    10,
    NULL,
    NULL,
    'Part A
47. Directions：
Suppose your university is going to host a summer camp for high school students. Write a notice to
1）briefly introduce the camp activities，and
2）call for volunteers.
You should write about 100words on the ANSERE SHEET.
Do not use your name or the name of your university.
Do not write your address.（10 points）',
    'Volunteers wanted
Volunteers are needed for the summer camp to be held on our campus in early July. The camp is meant to help lend participants a competitive edge over others so that they can better face challenges in the future.
Basic requirements are familiarity with the theme of the camp. Other requirements include interpersonal communication ability， familiarity with our university and the city in terms of their history， proper manners and sense of responsibility. Priority and preference will be given to those experienced， either in organizing camps or similar activities.
Call 123456 or send messages to 123456@abc for application and information of the interview. Inquiries and encouraged but visits declined.
ABC University',
    '假设你所在大学将为高中生举办夏令营。请写一则通知：1）简要介绍夏令营活动；2）招募志愿者。请在答题卡上写约100词。不要写姓名、学校名称或地址。（10分）',
    '通知
学生会将举办面向高中生的夏令营，旨在帮助他们开阔视野、体验不同的校园生活。由于部分外国学生也将参加，现面向全校招募志愿者。
夏令营计划于7月15日至7月22日举行，活动包括知识竞赛、爱国歌曲比赛、短剧表演等。我们希望志愿者热情负责，善于与外国学生沟通，并能积极参与各项活动。
如有兴趣，请尽快与我们联系。
学生会',
    '英一,小作文,notice,历年真题,范文',
    '有关文档/英语考研作文/英一/2015.md',
    1,
    20151
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2015_big',
    'english_one',
    2015,
    'big',
    'picture',
    '2015年英一大作文',
    20,
    160,
    200,
    'Part B
52. Directions:
Write an essay of 160-200 words based on the following drawing. In your essay you should 1) describe the drawing briefly 2) explain its intended meaning, and 3) give your comments
You should write neatly on the ANSWER SHEET. (20 points)',
    'A group of friends, boys or girls, are having a dinner party while each one of them is checking messages in their mobile phones without saying a word to one another, leaving the dishes untouched. We are informed that this is a gathering in the era of mobile phone.
The above picture unveils a common social phenomenon and the symbolic meaning of the photo is the effect of the mobile phone on people''s way of life. Undoubtedly, the phone provides us with considerable convenience, making many things possible which are beyond our dreams. As a communication tool, the phone makes us closer than ever before by providing immediate communication. Meanwhile, there are negative effects on our personal life. As is shown in the picture, people are imprisoned in their own world! They choose contacting online rather than communicating face to face.
Accordingly, enjoying the convenience provided by the phones, we should bear in mind that human beings are social beings who need real interpersonal interactions! Joint efforts are needed to ensure people to have face-to-face communication! I believe a harmonious relationship between friends is awaiting us if we set aside our mobile phones and enjoy the untouched meal!',
    '请根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请在答题卡上写约150词。（15分）',
    '图表反映了人们在聚餐时经常各自低头看手机、缺乏面对面交流的现象。
这种现象说明，手机在带来便利的同时，也在一定程度上影响了人与人之间的真实沟通。很多人习惯于在线联系，却忽略了身边亲友的感受。长此以往，不仅会削弱情感联系，也可能让本应温暖的聚会变得冷淡和疏离。
在我看来，我们当然应该享受科技带来的便利，但更应意识到真实交流的重要性。放下手机、认真倾听和陪伴身边的人，才是维系亲密关系的关键。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2015.md',
    1,
    20152
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2016_small',
    'english_one',
    2016,
    'small',
    'letter',
    '2016年英一小作文',
    10,
    100,
    100,
    'Part A
Directions:
Suppose you are a librarian in your university. Write a notice of about 100 words, providing the newly-enrolled international students with relevant information about the library.
You should write neatly on the ANWSER SHEET.
Do not sign you own name at the end of the letter, use “Li Ming ” instead.
Do not write the address .(10 points)',
    'To ensure students from overseas to be acquainted with the service of library in Beijing University, we write this notice to inform you of some relevant information about our library.
To begin with, our library provides a large amount of books and materials covering not only most majors and subjects, but also many extra-curricular reading materials, thus satisfying all your reading requirements. Furthermore, the library opens during the week time, each day from 9:00am to10:00pm. Last but not least, only students enrolled in this university and with a special Library Card are allowed to enter our library.
Anyone interested in studying or reading in our library should sign up before August 31, and the Library Card will be issued within a week.
Library of Beijing University
【解析】
今年英语（一）的小作文，再一次考查到了通知。在英语（一）的考试中，通知已经考查过1次。例如2010年英语（一）就考查过代表研究生会写一篇为国际会议招募志愿者的通知，而且2015年英语（二）考查的也是招募志愿者的通知。
同是以图书馆为写作背景，在2007年英语（一）的建议信中也已经考查过，所以相关表达在课堂上都已经涉及过。因此，只要根据2010年通知的格式，再结合2007年建议信的相关内容，就可以很轻松的写出2016年的小作文。这就提醒我们2017届的同学们要高度重视历年真题，因为考过的话题或是应用性短文会反复考查。
接下来，我们来详细解读今年的小作文。首先看一下题目要求：
Suppose you are a librarian in your university. Write a notice of about 100 words, providing the newly-enrolled international students with relevant information about the library.
小作文，即A节作文的评分侧重点在于：1. 格式和语域的恰当性；2. 信息点的覆盖面；3. 内容的组织；4. 语言的准确性。
在这里，我们主要从格式和内容两方面来解析此文。首先，从格式上来说，通知的格式包括四大部分：
1、标题，通知要求必须有标题，可以直接用Notice做标题，注意首字母大写，且标题要居中；
2、时间，通知的时间要求在标题下方第二行，按照日月年的顺序来写，注意月与年之间要有逗号，且时间要靠右对齐；
3、正文，通知正文要求首段缩进四个字符，不要求一定分段，但为了内容层次清晰，建议分三段为好。
4、落款，落款即发布通知的单位，此处需要特别注意，根据题目的要求，考生是代表图书馆写的通知，所以落款应该是某某图书馆，而非Li Ming本人。
另外，从内容上来看，作为一名图书管理员，要向学生介绍学校的图书馆，其实可以介绍的内容还有很多，比如图书馆的藏书、图书馆的开放时间、入馆要求等等。所以内容部分对大家来说应该也不是难事，具体内容可以参考如上范文。',
    '请写一则关于北京大学图书馆服务的介绍，帮助外国学生了解相关情况。请在答题卡上写约100词，不要署自己的真实姓名。（10分）',
    '为了帮助海外学生熟悉北京大学图书馆的服务，你需要向他们介绍图书借阅、阅览室开放时间、电子资源使用以及咨询方式等基本信息。文本的核心是帮助外国学生尽快了解校园图书馆的主要功能和使用规则，方便他们顺利开展学习与研究。',
    '英一,小作文,letter,历年真题,范文',
    '有关文档/英语考研作文/英一/2016.md',
    1,
    20161
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2016_big',
    'english_one',
    2016,
    'big',
    'picture',
    '2016年英一大作文',
    20,
    160,
    200,
    'Part B
52．Directions:
Write an essay of 160-200 words based on the following pictures． In your essay， you should
1) describe the pictures briefly
2) interpret the meaning ， and
3) give your comments
You should write neatly on the ANSWER SHEET．(20 points)
Do not sign your own name at the end of the letter． Use Li Ming instead．
Do not write the address． (10 points)',
    'What is symbolically depicted in the caricatures carries sharp contrast implications. In the first drawing, a father is watching a football match on the sofa. Meanwhile, he is supervising his son to finish homework. It is obvious that his son wears frowned expression on his face. On the contrary, the second portrayal depicts a father is working earnestly besides his son, and his son is doing his own assignment without prodding.
The drawer demonstrates that utmost significance should be attached to the phenomenon that setting proper examples has exerted great impact on the growth of the younger generation in China. Previously, it is widely acknowledged that parents are under obligation to help their kids form a set of appropriate values about the world and the life, which carries overwhelmingly precious connotation to the sound development on the younger generation. Simultaneously, there is no denying that the most rational method for adults to educate adolescents is to set them good examples rather than making perpetual requirements, which is less persuasive compared with the actions.
Accordingly, it is vital for us to derive positive implication from the drawings. On the one hand, we can frequently use them to enlighten parents to be more advisable in educating their children. On the other hand, parents should attach more emphasis on setting excellent models for their juveniles. Only in this way, can we effectively ensure a promising prospect for adolescents.',
    '请根据所给图画写一篇160-200词的短文。写作时你应当：1）简要描述图画；2）解释其寓意；3）发表你的评论。请工整地写在答题卡2上。（20分）',
    '漫画通过鲜明对比揭示了两种不同的心态：左边的人只有想法，没有行动；右边的人则一步步坚持实现自己的目标。
这幅图告诉我们，空想与拖延毫无价值，真正能带来改变的是持续行动。无论是在学习还是工作中，仅仅说“我要做”远远不够，关键在于把目标分解成具体步骤，并长期坚持下去。
因此，我认为成功往往不属于最会空谈的人，而属于那些愿意付诸实践、脚踏实地前进的人。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2016.md',
    1,
    20162
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2017_small',
    'english_one',
    2017,
    'small',
    'email',
    '2017年英一小作文',
    10,
    NULL,
    NULL,
    'Part A
51. Directions：
You are to write an email to James Cook, a newly-arrived Australian professor, recommending some tourist attractions in your city. Please give reasons for your recommendation.
You should write neatly on the ANSWER SHEET.
Do not sign your own name at the end of the email. Use “Li Ming” instead.
Do not write the address. (10 points)',
    'Dear James Cook,
Welcome to China! I''m writing this email to recommend some scenic spots in Beijing to you so that you can have a wonderful time here.
First of all, you could have a visit to the Summer Palace and Forbidden City, which are renowned for its old buildings and diverse cultures. After that, it is advisable to go to Wang Fujing, where you can have a taste of some delicious local food while enjoying the traditional culture. Finally, you can go to the National Museum, in which some traditional art exhibitions are being held. What do you think of my plans?
I sincerely hope that you could enjoy yourselves in Beijing and it will be my pleasure to be your guide.
Yours sincerely,',
    '请给 James Cook 写一封邮件，向他推荐一些中国景点并说明理由。请在答题卡上写约100词，不要署自己的真实姓名。（10分）',
    '亲爱的 James Cook：
欢迎来到中国！我写这封邮件，是想向你推荐几个值得一去的景点。
如果你喜欢历史文化，我建议你去故宫和长城；如果你更喜欢自然风光，桂林和九寨沟也是很好的选择。不同景点能够展示中国丰富多样的地理与文化面貌，相信会让你留下深刻印象。
希望这些建议能对你有所帮助，祝你旅途愉快。
你真诚的，
Li Ming',
    '英一,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英一/2017.md',
    1,
    20171
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2017_big',
    'english_one',
    2017,
    'big',
    'picture',
    '2017年英一大作文',
    20,
    160,
    200,
    'Part B
52. Directions：
Write an essay of 160-200 words based on the following pictures. In your essay, you should
1)describe the pictures briefly,
2)interpret the meaning , and
3)give your comments.
You should write neatly on the ANSWER SHEET. (20 points )',
    'Portrayed in the two cartoons is thought-provoking: In the left one, a young man, comfortably lounging on the chair, is looking at his huge bookshelf full of books contentedly, while the other, in the right drawing, is determined to finish reading 20 books in a year.
By this scenario, the cartoonist is trying to awaken us to the importance of reading and sticking to our goals. It is universally held that with the advance of modern society, only those equipped with updated knowledge which requires constant reading are most likely to reach the summit of the success. Conversely, without persistent learning and taking actions, our objectives are bound to be a fantasy. Indeed, people fail always because they stop trying, not because they encounter invincible difficulties.
From what has been mentioned above, we may reasonably arrive at the conclusion that only those who keep learning and cherish the spirit of persistence have opportunities to succeed. Therefore, such essence is an important virtue worthy of being fostered. If you understand and adhere to this principle in your study and work, you will definitely benefit greatly.',
    '请根据所给图画写一篇160-200词的短文。写作时你应当：1）简要描述图画；2）解释其寓意；3）发表你的评论。请工整地写在答题卡2上。（20分）',
    '两幅漫画形成鲜明对照：左边的人只是坐在椅子上满足地看着满书架的书，右边的人则下定决心一年读完20本书。
这组图想提醒我们，真正重要的不是拥有多少知识资源，而是是否愿意付诸行动、坚持阅读。随着社会不断发展，只有持续学习、不断更新知识，个人才更有可能取得成功。相反，如果只是停留在计划和想象层面，目标最终很容易沦为空谈。
因此，阅读习惯和坚持精神都值得培养。只有把想法落实为行动，才可能真正从书籍中受益。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2017.md',
    1,
    20172
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2018_small',
    'english_one',
    2018,
    'small',
    'email',
    '2018年英一小作文',
    10,
    100,
    100,
    'Part A 51. Directions： Write an email to all international experts on campus inviting them to attend the graduation ceremony. In your email you should include time, place and other relevant information about the ceremony. You should write about 100 words neatly on the ANSEWER SHEET Do not use your own name at the end of the email. Use “Li Ming” instead. (10 points)',
    'Dear Experts,
I am the chairman of the Students'' Union of our university. As we all know, our graduation ceremony is around the corner. And I am writing for the purpose of asking whether you can honor us to attend it.
You are admired by all the students and we would be grateful if you could be present at the ceremony to be held in our auditorium on June 26th, at 7 p.m. And on behalf of the university, I genuinely invite you to be our distinguished guests to award graduation certificates to students who have completed the required coursework at passing grades. If so, further details about the activity will be sent.
Your presences are cordially requested and appreciated, and I am looking forward to your replies at your earliest convenience.
Yours sincerely,
Li Ming',
    '请给校内所有国际专家写一封电子邮件，邀请他们参加毕业典礼，并在邮件中写明典礼的时间、地点以及其他相关信息。请在答题卡上写约100词。结尾不要署自己的名字，用“Li Ming”代替。（10分）',
    '亲爱的各位专家：
我谨代表学校诚挚邀请大家参加即将举行的毕业典礼。
典礼将于本周六上午九点在学校礼堂举行，届时将包括校长致辞、学生代表发言以及学位授予等环节。我们非常希望各位国际专家能够到场，与全体毕业生共同见证这一重要时刻。
感谢您一直以来对学校的支持，期待您的光临。
你真诚的，
Li Ming',
    '英一,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英一/2018.md',
    1,
    20181
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2018_big',
    'english_one',
    2018,
    'big',
    'picture',
    '2018年英一大作文',
    20,
    160,
    200,
    'Part B 52. Directions： Write an essay of 160-200 words based on the picture below. In your essay, you should 1)describe the pictures briefly 2)interpret the meaning and 3)give your comments(20 points) You should write neatly on the ANSWER SHEET. (20 points )',
    'In front of a desktop sits a young boy who is staring at the screen and selecting the curriculum. Apparently, a trace of hesitancy showed in his eyes. He is considering whether to choose the courses which are high scored, easily passed with less homework, or to choose those that contain new knowledge, focus on creating and are difficult to learn.
The implication echoed by this cartoon can be summarized as a philosophic topic in our daily life：the success of a man is closely related to the choice made by himself. Nevertheless, I cherish a belief that we cannot tell whether the selection is good or not, and as long as we adhere to our decision, success will be realized step by step. Although making a choice is essential to help determine the direction of our way, judged from the personal aspect, persistence functions as an indispensable driving force to keep up our spirit and to assist us to fulfill our study and work. However, some people, pacing up and down, are not industrious and try to find a short-cut success. In fact, only those who are hard-working and brave enough to encounter obstacles of all sorts are most likely to reach the summit of success.
Accordingly, persistence and making our own decision rationally are badly needed in every aspect of our society. In order to bring out this potential capability, our education system is supposed to be reformed fundamentally, focusing on cultivating the ability of confronting challenges. As an old saying goes:” No pain, no gain.”',
    '请根据所给图画写一篇160-200词的短文。写作时你应当：1）简要描述图画；2）解释其寓意；3）发表你的评论。请工整地写在答题卡2上。（20分）',
    '图画中，一个男孩坐在电脑前，正在“选择课程”，而旁边的家长似乎也在参与甚至主导这个过程。
这一场景反映出当代教育中一个值得关注的问题：在孩子成长和学习选择上，父母有时介入过多，甚至代替孩子作决定。虽然父母的出发点往往是关心和帮助，但如果缺乏边界，也可能削弱孩子独立思考和自主选择的能力。
在我看来，家庭教育最重要的不是包办一切，而是在支持与放手之间保持平衡。只有让年轻人逐步学会自己作决定，他们才能真正成长。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2018.md',
    1,
    20182
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2019_small',
    'english_one',
    2019,
    'small',
    'email',
    '2019年英一小作文',
    10,
    NULL,
    NULL,
    'Part A
51. Directions:
Suppse you are working for the “Aiding rurd Primary School” project of your university. Write an email to answer the inquiry from an international student volunteer, specifying details of the project.
Do not sign your own name at the end of the email. Use “Li Ming” instead.（10 points）',
    'Dear William,
I am exceedingly delighted to receive your letter in which you inquired something about the “Aiding Rural Primary Schools”. I, as a member of the project, am writing you this letter for the purpose of informing you some of the necessary information.
First and foremost, the theme of the project is to help those rural primary schools in remote areas by arranging some activities, such as sending various kinds of books about different aspects to those lovely students, which can not only broaden their horizon and enrich their life, but also cultivate their ability of thinking and learning independently and critically. In addition, the volunteers should have the character of diligence, coupled with an optimistic and pleasant personality, which will assure them of positive attitude when facing some setbacks during the process of these activities.
If you have any question about this project, please contact me without any hesitation at aidingruralprimaryschools@edu.cn.
Yours sincerely,
Li Ming',
    '假设你参与了本校“援助农村小学”项目。请给一位国际学生志愿者回信，介绍该项目的具体情况。结尾不要署自己的真实姓名，用“Li Ming”代替。（10分）',
    '亲爱的 William：
很高兴收到你的来信，得知你想了解“援助农村小学”项目的情况。我作为项目成员，愿意向你作简要介绍。
该项目旨在帮助偏远地区的农村小学，我们会通过赠送图书、组织活动等方式丰富孩子们的学习和生活，帮助他们拓展视野、培养独立思考能力。志愿者需要勤奋、乐观，并在活动过程中保持积极态度。
如果你还有其他问题，欢迎随时联系我。
你真诚的，
Li Ming',
    '英一,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英一/2019.md',
    1,
    20191
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2019_big',
    'english_one',
    2019,
    'big',
    'picture',
    '2019年英一大作文',
    20,
    NULL,
    NULL,
    'Part B
52. Directions:
Write an essay of 160—200 words based on the following pictures. In your essay, you should
1）describe the pictures briefly,
2）interpret the meaning, and
3）give your comments.
You should write neatly on the ANSWER SHEET. （20 points）',
    'As is vividly portrayed in the cartoon, two people are climbing the mountain together. One of them sits on a higher step with his backpack beside his feet and complains, "I''m tired, I don''t want to climb anymore." However, the partner carrying a bag catches up with him and hands him a bottle of water, he says, "Come on, take a break then continue to climb." Under the picture lies a caption: On the Way.
The impressive drawing has profoundly revealed that the process of our life is just like the mountain-climbing and only by adjusting our own pace and persisting in our goals can we reach a higher life realm——realizing the dreams for which we are always longing. For one thing, all kinds of interference make us give up sticking to dreams. For another, we haven''t integrated dreams into our life because of being anxious for success. The most common instance is that when we want to improve our health or obtain a better figure, majority of us may determine to do exercise, but often end in laziness and various excuses.
As the saying goes, the end of man is the beginning of God, only those who do their best can get the favor of heaven and enjoy the joy of victory. It is advisable for us, positive climbers, to view persistence and struggle as a lifestyle. Only by taking these approvals into action can we enjoy the richness and beauty of life.',
    '请根据所给图画写一篇160-200词的短文。写作时你应当：1）简要描述图画；2）解释其寓意；3）发表你的评论。请工整地写在答题卡上。（20分）',
    '漫画中，两个人一起爬山：前面的人走到较高处后疲惫地坐下，说自己不想再爬了；后面的人一边赶上来，一边递水并鼓励他继续前行。画面下方写着“在路上”。
这幅图生动地揭示出：人生就像登山，通往目标的过程往往伴随着疲惫、犹豫和困难，但只要适时调整节奏、彼此鼓励并坚持下去，就有希望到达更高的境界。现实中，很多人放弃理想并不是因为目标不重要，而是因为在困难面前缺少耐心与毅力。
因此，我认为坚持和奋斗应成为一种生活方式。只有持续前进，我们才更有可能享受成功带来的喜悦。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2019.md',
    1,
    20192
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2020_small',
    'english_one',
    2020,
    'small',
    'notice',
    '2020年英一小作文',
    10,
    100,
    100,
    'Part A
The student union of your university has assigned you to inform the international students
about an upcoming singing contest.Write a notice in about 100 words.
Write your answer on the ANSWER SHEET.
Do not use your own name in the notice.(10 points)',
    'In order to enrich the campus life and provide the colorful life for you,the Students'' Union is preparing the upcoming singing contest, which will be held in the auditorium in our university on the evening of December 31, 2019. Now, the Union is recruiting contestants for this competition.
Anyone who are fond of signing or interested in the competition, please send his or her application to students'' union @ sohu.com before next Wednesday.Besides,there are generous awards in gratitude for this activity.Please do not hesitate to contact us if you have any queries concerming the singing contest. Meanwhile, volunteers for this activity are badly needed to assist us in organizing the relevant affairs.
We are looking forward to your participation.
The Students'' Union',
    '请写一则通知，介绍一门旨在丰富校园生活、为学生提供多彩体验的课程或活动信息。请在答题卡上写约100词，不要署自己的真实姓名。（10分）',
    '为了丰富校园生活，并为同学们提供更加多彩的体验，学生会将组织一项新的校园活动。该活动面向全体同学开放，欢迎大家积极参加。通过参与，同学们不仅可以放松心情，还能拓展兴趣、结识朋友并提升综合素质。具体时间、地点和报名方式请关注后续通知。',
    '英一,小作文,notice,历年真题,范文',
    '有关文档/英语考研作文/英一/2020.md',
    1,
    20201
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2020_big',
    'english_one',
    2020,
    'big',
    'picture',
    '2020年英一大作文',
    20,
    160,
    200,
    'Part B
52. Directions:
Write an essay of 160-200 words based on the pictures below.In your essay,you should
1) describe the picture briefly,
2) interpret the implied meaning. am
3) give your comments.
Write your answer on the ANSWER SHEET. (20 points)
### 解析/补充说明
解析：
2020年考研英语已经结束啦，相信同学们在考场上看到考题会信心满满。因为从内容上来讲，今年的英语一大作文依然延续2019年的出题思路，考查了品质类话题的图画作文，而不同的地方在于2019年是一幅图，但今年却是两幅图相反。从难度上来说，今年和去年持平，对于“习惯”的英语表达，相信大家都不陌生是“habit”。
写作时，按照一般顺序。第一段图画描述段，左图是位女孩正在非常努力地做着学校作业，心里想着是“尽早完成才放心”，右图是位男孩躺在沙发上也不看书，书桌上只有一本摊开的书和一支笔，心里却想着“不到最后不动手”，这两幅所反映的内容正好相反，它所反映的寓意依然是合理自洽即可，小编认为两者的不同在于习惯，女孩有很好的规划能力并有效利用时间完成任务，而男孩则恰恰相反，空耗时间只待最后“速成”，结果自然不会理想，因而我们需要养成合理规划时间的好习惯;第二段是寓意阐释段，按照品质类的框架结构，即关键句和具体作用, 可以说scheduling有助于我们充分利用时间以达到学业乃至事业上的成功，还可以讲acquire the habit of time management有助于我们克服拖延，做时间的主人;第三段是个人评论段，框架结构是关键句+具体建议+结尾句，可以说我们需要加强人们对此种品质的意识，希望人人都可以拥有这种美好的品质。
另外，注意大作文的评分侧重点。考研大纲指出：B节作文的评分侧重点在于信息点的完整、内容组织的连贯、语言的准确性、语言的多样性。字迹工整，少涂抹痕迹也很关键哦。
下面附加范文以供小可爱们参考。',
    'Portrayed distinctively by the two cartoons above is an impressive scene: a girl in the left picture is doing homework and saying that early completion is better. Nevertheless, the boy in the right picture is sitting in front of the desk and saying that he will not finish the homework until the last minute.
Undoubtedly, the symbolic implication of the pictures is to show us that importance should be attached to the formation of good habits, especially the good habit of time management.On the one hand, efficient time management is critical to personal development.As the old saying goes,“Time is money," and in the fast-paced modern life, it seems that we always have a lot of things to do and we are very busy. In the face of such a situation, we have to realize that efficiency holds the key to saving time and time management skills hold the key to personal success. On the other hand,good time management habits play a vital role in the development of the whole society.There is no doubt that, to a large extent, social progress is closely related to the efforts of each individual. If we can develop the good habit of time management,we are much more likely to improve efficiency and have a better performance in the learming and working process, which is an integral part of social advances and prosperity.
From what has been mentioned above, we can come to the conclusion that the sense of efficient time management skills is of equal importance in personal and social progress.Therefore,we ought to take advantage of the phenomenon to enlighten the public and the press is expected to take a lead in advertising the value of developing good time management habits.Only in this way can we have a bright future.',
    '请根据所给图画写一篇160-200词的短文。写作时你应当：1）简要描述图画；2）解释其寓意；3）发表你的评论。请工整地写在答题卡上。（20分）',
    '两幅漫画形成对照：左边的女孩穿着传统服饰、认真学习戏曲；右边的女孩则戴着耳机、沉浸在流行文化中。这一对比反映了传统文化与现代娱乐在年轻人中的不同吸引力。
这并不意味着传统文化失去了价值，而是说明在快速变化的时代背景下，它需要以更适合当代传播的方式重新走近年轻人。传统艺术承载着民族记忆和文化精神，而流行文化则更贴近日常生活。二者之间并非只能对立，而是可以相互融合、共同发展。
我认为，保护和传承传统文化并不是简单重复旧形式，而是要让它在当代语境中重新焕发生命力。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2020.md',
    1,
    20202
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2021_small',
    'english_one',
    2021,
    'small',
    'email',
    '2021年英一小作文',
    10,
    100,
    100,
    'Part A (10 points)
【题干】Directions:
A foreign friend of yours has recently graduated from college and intends to find a job in China. Write him/her an email to make some suggestions.
You should write about 100 words on ANSWER SHEET 2.
### 解析/补充说明
Do not sign your own name at the end. Use "Li Ming Open" instead.
You do not need to write the address.',
    'Dear friend,
Hope this letter finds you well I am glad to hear you intend to find a job in China, so I would like to extend my warmest welcome as well as provide you with a few suggestions on job-hunting.
First, you can start from listing 3 to 5 cities which you would like to work or live in To be more specific, rate them by location, working opportunities and prospects and, of course the city''s happiness level. What''s more, be prepared for the culture shock. There is a sharp contrast in how eastern people and western people work. The former prefers working individually while the latter is prone to teamwork. There is one more point that, I suppose I have to touch on: make good use of online job-hunting applications, such as BOSS and 51Job.
I hope you will find my humble suggestions be of help. I am looking forward to your reply. Best wishes.
Yours,
Li Ming',
    '你的朋友希望找一份暑期工作。请给他写一封信：1）提供建议；2）说明理由。请在答题卡上写约100词。不要署自己的真实姓名，用“Li Ming”代替。（10分）',
    '亲爱的朋友：
希望你一切都好。得知你想找一份暑期工作，我很高兴给你一些建议。
我认为你可以优先考虑与所学专业相关的实习或服务性工作，因为这类工作既能让你积累实践经验，也能帮助你了解真实的工作环境。同时，你还可以在工作中提高沟通能力、责任感和时间管理能力。
无论最终选择什么岗位，最重要的是保持认真和积极的态度。祝你找到一份满意的暑期工作。
你真诚的，
Li Ming',
    '英一,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英一/2021.md',
    1,
    20211
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2021_big',
    'english_one',
    2021,
    'big',
    'picture',
    '2021年英一大作文',
    15,
    160,
    200,
    'Part B (15 points)
【题干】Directions:
Write an essay of 160-200 words based on the following drawing. In your essay, you should
1) describe the drawing briefly,
2) explain its intended meaning, and then
3) give your comments.
You should write neatly on ANSWER SHEET 2.',
    'What is graphically and explicitly depicted in the simple yet eye-catching drawing is that on the ground stands a father, who is having a talk with his son. Impressively, at second glance, it is not difficult to observe that the boy, dressed in a traditional Chinese costume, expresses his concern about studying drama, while his father offer some words of encouragement.
Without a doubt, no boy who was born and raised in China could be ignorant that China is an ancient nation with a long history and splendid traditional culture. Traditional dramas, like Peking opera, are the national essence of our culture, which are not only part of the national heritage, but also part of a living and continuing culture. However, traditional culture has been subject to the impact and damage caused by network culture. It is a not uncommon occurrence that quite a few people show too little enthusiasm for traditional dramas. Instead, they are more than willing to follow the popular culture.
While popular culture is completely transforming people''s thoughts and ways of thinking, we are supposed to cherish the roots of national culture and build cultural confidence. Accordingly, it is my view that national culture should be preserved and cherished as priceless spiritual treasure.',
    '请根据所给图画写一篇160-200词的短文。写作时你应当：1）简要描述图画；2）解释其寓意；3）发表你的评论。请工整地写在答题卡上。（20分）',
    '图画展示了一个看似简单却意义明确的场景：人们在不同环境中依然坚持做该做的事情，表现出一种稳定而自觉的行动力。
它所传递的核心含义在于，自律和坚持是个人成长中最宝贵的品质之一。许多目标之所以难以实现，并不是因为能力不足，而是因为缺乏长期稳定的行动。真正决定人与人差距的，往往不是一时的热情，而是日复一日的坚持。
在我看来，自律不是束缚，而是一种让人获得自由和进步的能力。只有学会约束自己，我们才能更接近理想中的人生状态。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2021.md',
    1,
    20212
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2022_small',
    'english_one',
    2022,
    'small',
    'email',
    '2022年英一小作文',
    10,
    100,
    100,
    'Part A
51. Directions:
Write a email to a professor at a British university, inviting him/her to organize a team for the international innovation to be held at your university. You should write about 100 words on ANSWER SHEET.
Do not sign your own name at the end of the letter. Use "Li Ming" instead. Do not write the address. (10 points)',
    'Dear Sir or Madam,
As an orgaizer of the international innovation, I am writing this letter to invite you to organize a team here.
First and foremost, the international innovation will be held in the Auditorium on campus on Dec.25th,2021 at 9:00 am. Secondly, as the key member of the team, we hope you could do the following things: scoring the potential participants, making comments for them, and giving them advanced advice. What''s more, due to the pandemic, a nucleic acid test ought to be carried out. Members of the Students'' Union will be there to provide you necessary guidance.
I would appreciate if you could take my invitation into account. Thank you for your time and attention. Best regards! Yours sincerely,
Li Ming',
    '请给国际创新大会的参会者写一封信，介绍相关安排并表达欢迎。请在答题卡上写约100词。结尾不要署自己的名字，用“Li Ming”代替。（10分）',
    '尊敬的先生/女士：
作为国际创新大会的组织者之一，我写信是想向您介绍有关安排，并向您表示热烈欢迎。
本次大会将汇集来自不同国家和地区的参会者，共同探讨创新与合作。会议期间将安排主题发言、小组讨论和交流环节，希望各位来宾能够积极参与，分享真知灼见。
感谢您对本次大会的支持，期待您的到来。
你真诚的，
Li Ming',
    '英一,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英一/2022.md',
    1,
    20221
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2022_big',
    'english_one',
    2022,
    'big',
    'picture',
    '2022年英一大作文',
    20,
    NULL,
    NULL,
    'Part B
52. Directions:
Write an essay of 160—200 words based on the following drawing. In your essay, you should
1) describe the drawing briefly,
2) explain its intended meaning, and
3) give your comments.
You should write neatly on ANSWER SHIEET. (20 points)
### 解析/补充说明
【图片描述】
两个学生站在一张校园讲座的海报前，其中一个说，不是我们专业的，听了也没多大用。另一个说，听了或许也有用。【段落字数】57+108+33=198字',
    'Clearly, the picture above shows students'' different attitudes towards campus lectures. As is vividly shown in the picture, the girl on the left complains, "We will not benefit much from the lecture, because it''s not related to our major." However, the girl on the right says, "We will learn something useful as long as we attend it."
Apparently, the picture tells us that college students shouldn''t only focus on their own majors, and the reasons are as follows. On the one hand, students with comprehensive knowledge and skills are more preferred in the job market. That is to say, compared with those who only master the knowledge in their specific field, those who are fully developed have more job opportunities and greater career potential. On the other hand, if they only pay attention to their own subjects and neglect the knowledge of other fields, it is hard for them to apply all sorts of knowledge to their future life and work in a comprehensive way.
Therefore, it is necessary for college students to achieve comprehensive development by acquiring various kinds of knowledge and skills. Only in this way can they make greater progress and have a brighter future.',
    '请根据所给图画写一篇160-200词的短文。写作时你应当：1）简要描述图画；2）解释其寓意；3）发表你的评论。请工整地写在答题卡上。（20分）',
    '图画展示了学生对校园讲座的不同态度：有人热情参与，有人漠不关心，这种差异十分明显。
这一现象说明，同样的教育资源并不会被所有人同等重视。积极参与讲座的学生通常希望借此拓展知识面、接触新观点；而态度消极的人则可能只关注眼前任务，忽视了校园活动对个人成长的长期价值。
我认为，大学教育不仅发生在课堂内，也发生在各种讲座、讨论与交流之中。讲座的意义不仅是“听一场报告”，更在于培养开放视野和主动学习的能力。因此，学生应当珍惜此类机会。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2022.md',
    1,
    20222
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2023_small',
    'english_one',
    2023,
    'small',
    'notice',
    '2023年英一小作文',
    10,
    100,
    100,
    'Part A
51. Directions: Write a notice to recruit a student for Prof. Smith''s research project on campus sports activities. Specify the duties and requirements of the job.
You should write about 100 words on the ANSWER SHEET.',
    'Notice
A student is wanted for Professor Smith''s research project on campus sports activities.
The selected student will help collect and organize data on sports events on campus, interview student athletes and relevant staff members, and assist the research team in arranging regular meetings and preparing reports. Applicants should be responsible, well organized, patient, and genuinely interested in sports and research work.
If you would like to join the project, please send your application to email@123.com.
Students'' Union',
    '请写一则通知，为 Smith 教授关于校园体育活动的研究项目招募一名学生。通知中应说明工作职责和岗位要求。请在答题卡上写约100词。（10分）',
    '通知
现招募一名学生参与 Smith 教授关于校园体育活动的研究项目。
该岗位的主要职责包括：收集校园体育活动相关数据，协助研究团队进行整理与分析；观察不同体育项目的开展情况；采访学生运动员、教师或工作人员并记录相关信息。此外，入选者还需要配合组织与 Smith 教授及研究团队成员的阶段性沟通会议，汇报研究进展。
应聘者应具备较强的组织能力、耐心以及对体育活动的兴趣。如有意申请，请通过 email@123.com 与我们联系。
学生会',
    '英一,小作文,notice,历年真题,范文',
    '有关文档/英语考研作文/英一/2023.md',
    1,
    20231
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2023_big',
    'english_one',
    2023,
    'big',
    'picture',
    '2023年英一大作文',
    20,
    160,
    200,
    '52. Directions:
Write an essay of 160-200 words based on the picture below. In your essay , you should
1) describe the picture briefly,
2 ) interpret the implied meaning , and
3) give your comments.
You should write neatly on the ANSWER SHEET. ( 20 points)',
    'The picture shows an exciting dragon-boat race during a traditional Chinese festival. The rowers are working together with great effort, while many young people on the bank are cheering enthusiastically. An elderly couple nearby are pleased to see such lively traditional celebrations.
What the picture suggests is that more and more young people are becoming interested in traditional culture and are willing to take part in related activities. In recent years, traditional festivals and customs have regained popularity, partly because of social media, school education and growing cultural confidence among the younger generation.
In my view, this is an encouraging trend. Traditional culture can truly survive only when young people are not merely aware of it, but actively participate in and pass it on.',
    '请根据所给图画写一篇160-200词的短文。写作时你应当：1）简要描述图画；2）解释其寓意；3）发表你的评论。请工整地写在答题卡上。（20分）',
    '图画描绘了中国传统节日中的龙舟赛场景：赛手齐心划桨，岸边不少年轻人为比赛呐喊助威，一对老年夫妇则欣慰地感叹这些年传统活动越来越精彩。
画面所体现的核心含义是：越来越多年轻人正在主动接近并参与传统文化与节日活动。如今，在社交媒体传播和学校教育推动下，传统文化以更生动、更新颖的方式重新走入公众生活。年轻一代不再只是“知道”传统节日，而是愿意真正参与其中、体验其中。
在我看来，这是一种令人振奋的文化现象。只有当年轻人愿意继承并发展传统文化，它才会真正拥有持续的生命力。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2023.md',
    1,
    20232
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2024_small',
    'english_one',
    2024,
    'small',
    'email',
    '2024年英一小作文',
    10,
    100,
    100,
    'Part A
51. Directions:
Read the following email from an intemational student and write a reply.
Dear Li Ming, I''ve got a class assignment to make an oral report on an ancient Chinese scientist, but I''m not sure how to prepare for it. Can you give me some advice? Thank you for your help.
Yours,
Paul
Write your answer in about 100 words on the ANSWER SHEET.
Do not use your own name in your email; use "Li Ming" instead.(10 points)',
    'Dear Paul,
I am extremely delighted to hear from you. In response to your request, I am writing for the purpose of offering some practical suggestions on how to make preparations for your oral report.
The relevant proposals are mainly as follows. First and foremost, it is highly suggested that you take full advantage of some reference books or browse the Internet to select one of your favorite ancient Chinese scientists. In addition, it is advisable for you to form a systematic view of his story by learning about his life and some of his representative works. Furthermore, it would be better if you could practice oral speaking skills in advance so as to stand out from all the other participants.
Please feel free to contact me if you have any further questions. I am looking forward to your performance.
Yours,
Li Ming',
    '请阅读国际学生 Paul 的邮件并给他回信。邮件中他说他要做一份关于中国古代科学家的英语口头报告，但不知道该如何准备，希望你给他一些建议。请在答题卡上写约100词。不要在邮件中使用自己的名字，改用“Li Ming”。（10分）',
    '亲爱的 Paul：
很高兴收到你的来信。针对你提出的问题，我写这封邮件是想给你一些关于口头报告准备的实用建议。
首先，建议你充分利用参考书或网络资料，从中国古代科学家中挑选一位你最感兴趣的人物。其次，你最好系统了解他的生平经历以及具有代表性的成就或作品，这样能让你的报告内容更加完整。最后，如果你能提前练习口头表达，你的展示效果会更出色。
如果你还有其他问题，欢迎随时联系我。期待你的精彩表现。
你真诚的，
Li Ming',
    '英一,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英一/2024.md',
    1,
    20241
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2024_big',
    'english_one',
    2024,
    'big',
    'chart',
    '2024年英一大作文',
    20,
    160,
    200,
    '52. Directions:
Write an essay based on the picture and the chart below. In your essay, you should
1) describe the picture and the chart briefly,
2) interpret the implied meaning, and
3) give your comments.
Write your answer in 160-200 words on the ANSWER SHEET.(20 points)',
    'The picture and the chart show a positive change in urban public space. The chart indicates that the number of parks rose from 406 in 2020 to 670 in 2022. In the picture, several young people are running past a fitness area in a park, while a local resident expresses satisfaction with the construction of parks.
The message is that better public facilities can directly improve people''s quality of life. As cities pay more attention to livability, parks are no longer simple green decorations; they have become important places for exercise, relaxation and social interaction. More parks mean more opportunities for residents to enjoy healthy and convenient daily life.
In my opinion, this trend deserves strong support. Building more parks benefits individuals physically and mentally, and it also reflects a more people-centered approach to urban development.',
    '请根据图片和图表写一篇短文。写作时你应当：1）简要描述图片和图表；2）解释其含义；3）发表你的评论。请在答题卡上写160-200词。（20分）',
    '图表显示，2020年至2022年间公园数量由406增长到670；画面中，一群年轻人从公园健身区跑过，旁边居民对公园建设表示满意。
这一现象反映出城市生活环境正在持续改善。首先，越来越多公园和健身空间的建设说明政府更加重视宜居城市和公共服务。其次，公园不仅提供休闲景观，也为市民特别是年轻人和老年人提供了锻炼与交流的场所，因此更容易提升居民的幸福感。
在我看来，增加公共绿地和公园数量是非常有价值的城市发展方向。它既有利于个体健康，也有助于提升整个城市的品质与吸引力。',
    '英一,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英一/2024.md',
    1,
    20242
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2025_small',
    'english_one',
    2025,
    'small',
    'email',
    '2025年英一小作文',
    10,
    100,
    100,
    'Part A
51. Directions:
Read the following email from your classmate Paul and write him a reply.
Dear Li Ming,
I was really excited to hear that you’d invite some young craftsmen to demonstrate their innovative craft-making on campus. May I know more about what they’ll show? Also, I’d like to help with your presentation work. Please let me know what I can do.
Yours,
Paul
Write your answer in about 100 words on the ANSWER SHEET.
Do not use your own name in the email; use “Li Ming” instead. (10 points)',
    'Dear Paul,
I’m glad to hear you’re excited about the craft-making show. Therefore, I am writing this email with the purpose of tell you some detailed information about this event.
For a start, the young craftsmen we’ve invited will showcase a mix of traditional and modern crafts. For example, there will be demonstrations of paper-cutting, pottery-making, and embroidery, as well as some innovative works like 3D-printed crafts and eco-friendly handmade products. We hope this variety will inspire everyone to appreciate the blending of traditional skills and modern creativity. Besides, as for your offer to help, it would be great if you could assist with preparing display boards with information about the craftsmen and their works, organizing the seating arrangements, and setting up the demonstration area. If you have any other ideas or suggestions, please feel free to share them!
Thank you again for your support—I’m really looking forward to working with you to make this event a success!
Yours sincerely,
Li Ming',
    '请阅读同学 Paul 的邮件，并给他写一封回信。邮件中他询问你们邀请年轻手工艺人来校展示创新手作的活动内容，并表示愿意协助展示工作。请在答题卡上写约100词。不要在邮件中使用自己的名字，改用“Li Ming”。（10分）',
    '亲爱的 Paul：
很高兴听到你对这场手工艺展示活动感到兴奋。因此，我写这封邮件，向你介绍一些更具体的信息。
首先，我们邀请的年轻手工艺人将展示传统与现代结合的多种作品，例如剪纸、陶艺、刺绣，以及3D打印手工品和环保手作产品。至于你提出愿意帮忙，如果你能协助准备介绍展板、安排座位并布置展示区域，那将非常棒。如果你还有其他主意，也欢迎随时提出。
再次感谢你的支持，我非常期待与你一起把这次活动办成功。
你真诚的，
Li Ming',
    '英一,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英一/2025.md',
    1,
    20251
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_one_2025_big',
    'english_one',
    2025,
    'big',
    'picture',
    '2025年英一大作文',
    20,
    160,
    200,
    'Part B
52. Directions:
Write an essay of 160-200 words based on the following drawing. In your essay you should
1) describe the drawing briefly,
2) explain its intended meaning, and
3) give your comments.
You should write neatly on the ANSWER SHEET. (20 points)
表格：近年来全国居民平均每百户年末主要耐用消费品拥有量
| 年 | 空调（台） | 洗衣机（台） | 电冰箱（柜）（台） |
| --- | --- | --- | --- |
| 2014 | 75.2 | 83.7 | 85.5 |
| 2017 | 96.1 | 91.7 | 95.3 |
| 2020 | 117.7 | 96.7 | 101.8 |
| 2023 | 145.9 | 98.2 | 103.4 |',
    'The table above clearly presented some noticeable data with regard to the average ownership of major durable consumer goods per 100 households in China from 2014 to 2023. To be specific, the ownership of air-conditioners per 100 households increased markedly from 75.2 in 2014 to 145.9 in 2023, with that of washing machines experiencing a moderate growth from 83.7 to 98.2 during the same period. Meanwhile, there was a rise by 17.9% in terms of the ownership of refrigerators during the past 10 years.
We are supposed to place our attention on, instead of its appearance, the trend reflected in the chart. Why does this phenomenon appear? Several possible reasons, from my perspective, can be responsible for this. First and foremost, the trend points to overall economic growth and rising living standards. The increasing ownership of such appliances suggests improved household incomes, enabling families to afford these goods that enhance comfort and convenience. Additionally, this established phenomenon is directly bound up with the advances in technology and accessibility. Manufacturers have likely made durable goods more efficient, affordable, and widely available, catering to the needs of an expanding middle class.
Given the analysis above, we can definitely arrive at a conclusion that this established trend is normal and acceptable. Besides, this phenomenon will surely continue for quite a while in the forthcoming years.',
    '请根据所给图表写一篇160-200词的短文。写作时你应当：1）简要描述图表；2）解释其含义；3）发表你的评论。请工整地写在答题卡上。（20分）',
    '图表展示了2014年至2023年间我国居民每百户耐用消费品拥有量的变化。空调数量由75.2台上升到145.9台，洗衣机由83.7台增至98.2台，冰箱也有所增长。
这些数字反映出居民生活水平的整体提升。首先，家庭收入增加使人们更有能力购买能够提升生活舒适度的家用电器。其次，技术进步和市场普及让这些耐用品更加高效、实用且容易获得，从而进一步推动了消费增长。
总的来说，这一变化是经济发展和生活改善的自然结果。未来随着居民需求升级，相关耐用消费品的普及仍可能继续推进。',
    '英一,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英一/2025.md',
    1,
    20252
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2010_small',
    'english_two',
    2010,
    'small',
    'letter',
    '2010年英二小作文',
    10,
    100,
    100,
    'You have just come back from the U. S. as a member of a Sino-American cultural exchange program. Write a letter to your American colleague to 1) express your thanks for his/her warm reception; 2) welcome him/her to visit China in due course. You should write about 100 words on ANSWER SHEET. Do not sign your own name at the end of the letter. Use "Zhang Wei" instead. Do not write your address. (10 points)',
    'Dear Judy,
I would like to convey my heartfelt thanks to you for your warm reception when I participated in the exchange program in your country.
Your generous help made it possible for me to have a very pleasant stay and a chance to know American culture better. Besides, I think it is a great honor for me to make friends with you and I will cherish the goodwill you showed to me wherever I go. I do hope that you will visit China one day, so that I could have the opportunity to repay your kindness and refresh our friendship.
I feel obliged to thank you again. Wish you all the best.
Yours sincerely,
Zhang Wei',
    '你刚作为中美文化交流项目的一员从美国回来。请给你的美国同事写一封信：1）感谢他/她的热情接待；2）欢迎他/她在适当的时候访问中国。请在答题卡上写约100词。结尾不要署自己的名字，用“Zhang Wei”代替。不要写地址。（10分）',
    '亲爱的 Judy：
我想衷心感谢你在我参加贵国交流项目期间给予的热情接待。
你的慷慨帮助让我度过了一段非常愉快的时光，也让我有机会更深入地了解美国文化。此外，能与你成为朋友我深感荣幸。无论走到哪里，我都会珍惜你对我的善意。我真诚希望你有一天能来中国访问，这样我就有机会回报你的 kindness，也让我们的友谊更加深厚。
再次向你表示感谢，祝你一切顺利。
你真诚的，
Zhang Wei',
    '英二,小作文,letter,历年真题,范文',
    '有关文档/英语考研作文/英二/2010.md',
    1,
    20101
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2010_big',
    'english_two',
    2010,
    'big',
    'chart',
    '2010年英二大作文',
    15,
    NULL,
    NULL,
    'In this section, you are asked to write an essay based on the following chart. In your writing, you should 1) interpret the chart, and 2) give your comments. Write your essay on ANSWER SHEET 2. (15 points)',
    'The column chart on mobile-phone subscriptions above shows a striking contrast between those developing countries and developed countries in the past decade. Since 2004, people in growing numbers in developing countries use mobile phones, while those in developed countries far lag behind. By 2008, there are 4 billion mobile-phone users in developing countries, which is 3 times more than that in developed countries.
Two reasons may contribute to the contrast above. On the one hand, with the rapid increase in economy of developing countries, the telecommunication industry there surges to meet the demand of globalization. Therefore, mobile-phone users grow at an incredible speed. On the other hand, developed countries had reached a state of prosperity in economy, and created less room for further developments in mobile subscriptions.
To sum up, mobile phones are convenient for interpersonal communication and gradually become indispensable tools in people''s life.',
    '本部分要求你根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请将作文写在答题卡2上。（15分）',
    '上面的手机用户数量柱状图显示，在过去十年里，发展中国家和发达国家之间形成了鲜明对比。自2004年以来，发展中国家的手机使用人数迅速增长，而发达国家的增长速度明显较慢。到2008年，发展中国家的手机用户已达到40亿，是发达国家的3倍。
造成这一差异的原因主要有两点。一方面，随着发展中国家经济快速增长，电信行业为了适应全球化需求而迅速扩张，因此手机用户数量以惊人的速度增加。另一方面，发达国家经济发展较早，市场相对饱和，移动通信用户继续大幅增长的空间有限。
总之，手机为人际交流提供了便利，并逐渐成为人们生活中不可缺少的工具。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2010.md',
    1,
    20102
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2011_small',
    'english_two',
    2011,
    'small',
    'letter',
    '2011年英二小作文',
    10,
    100,
    100,
    'Directions:
Suppose your cousin Li Ming has just been admitted to a university. Write
him/her a letter to
congratulate him/her, and
give him/her suggestions on how to get prepared for university life.
You should write about 100 words on the ANSWER SHEET.
Do not sign your own name at the end of the letter. Use “Zhang Wei”
instead.
Do not write the address. (10 points)',
    'Dear Li Ming,
I’m writing the letter for the purpose of conveying my heartfelt
congratulations to you on your success in passing the entrance examination. And
I’d like to make some suggestions on your forthcoming university life.
To begin with, it’s advisable for you to be equipped with computer skills,
considering that we’re now in a high-tech age. Besides, I would recommend that
you continue studying English in your spare time, which will lay a solid
foundation for your future study abroad. Lastly, emphasis should be given to
communication skills as well as academic achievements.
I genuinely hope that you’ll take my advice into consideration. Wish you a
splendid university life.
Yours sincerely,
Zhang Wei',
    '假设你的表弟（表妹）Li Ming刚被大学录取。请给他/她写一封信：1）表示祝贺；2）就如何为大学生活做准备提出建议。请在答题卡上写约100词。结尾不要署自己的名字，用“Zhang Wei”代替。不要写地址。（10分）',
    '亲爱的 Li Ming：
我写这封信，是想衷心祝贺你顺利通过入学考试，成功被大学录取。同时，我也想就你即将开始的大学生活给你一些建议。
首先，你最好尽快掌握一些基本的计算机技能，因为无论学习还是今后的生活都会经常用到。其次，你可以利用假期多读一些书，提前了解所学专业，并培养良好的自学习惯。最后，也别忘了锻炼身体、调整心态，以便更快适应全新的校园环境。
祝你大学生活精彩充实。
你真诚的，
Zhang Wei',
    '英二,小作文,letter,历年真题,范文',
    '有关文档/英语考研作文/英二/2011.md',
    1,
    20111
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2011_big',
    'english_two',
    2011,
    'big',
    'chart',
    '2011年英二大作文',
    15,
    NULL,
    NULL,
    'Directions:
Write an essay based on the following chart. In you writing, you should
interpret the chart and
give your comments.
You should write at least 150 words.
Write your essay on the ANSWER SHEET. (15 points)',
    'As is shown in this chart, the market share owned by domestic car brands has
increased a lot, climbing nearly to 35% in 2009. In the meaning time, the market
percentage of Japanese cars has witnessed a dramatic slump, dropping by 10% from
2008 to 2009. In comparison, the share of American cars has remained almost
constant.
The contributing factors for this phenomenon can be summarized as follows.
The first and fore-most one is the rapid development of the Chinese motor
industry. According to a recent official report, many Chinese car brands have
not only captured more markets but made lots of breakthroughs in technology. In
addition, we cannot deny the fact that home-made cars enjoy an advantage in
price so they can be afforded by more consumers. The final factor that I’d like
to highlight here is that, with the advance of the Chinese society, Chinese
consumers have changed their prejudiced outlook on domestic car brands.
Considering what has been discussed above, we may safely draw the conclusion
that the present situation is quite normal. And without any doubt, this tendency
will go on in the years to come.',
    '请根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请至少写150词，并写在答题卡上。（15分）',
    '正如图表所示，2008年至2009年间，国产汽车品牌的市场份额由25%上升到近30%，增长趋势较为明显。
造成这一现象的原因并不难理解。首先，随着我国汽车工业的发展，国产品牌在技术、质量和外观设计等方面都有了显著提升，因此更容易赢得消费者信任。其次，国产车在价格、售后服务以及维修成本方面更具优势，能够更好地满足普通家庭的实际需求。
总体来看，这一趋势说明国产汽车品牌的竞争力正在增强，未来只要继续提升质量与创新能力，它们仍有望获得更大的市场空间。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2011.md',
    1,
    20112
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2012_small',
    'english_two',
    2012,
    'small',
    'letter',
    '2012年英二小作文',
    10,
    100,
    100,
    'Directions:
Suppose you have found something wrong with the electronic dictionary that you bought from an online store
the other day. Write an e-mail to the customer service center to
make a complaint and
demand a prompt solution.
You should write about 100 words on the ANSWER SHEET.
Do not sign your own name at the end of the letter. Use “Zhang Wei” instead. Do not write the address. (10 points)',
    'Dear Sir or Madam,
I’m writing the letter for the purpose of making a complaint about the flaws of your product---an electronic dictionary which I bought from your online store the other day.
The problems I’ve found are as follows. To begin with, it often breaks down for no reason, which gives much inconvenience and trouble to my use. In addition, its screen is covered with a few scratches. Lastly, some of the keys on the keyboard fail to work well.
Considering these factors, I strongly request that you should send me a new one or refund me the money. I would appreciate it a lot if you could take my complaint seriously and see to it promptly. Looking forward to your reply at your earliest convenience.
Yours sincerely,
Zhang Wei',
    '假设你前几天在一家网店买了一部电子词典，后来发现有问题。请给客服中心写一封电子邮件：1）提出投诉；2）要求尽快解决。请在答题卡上写约100词。结尾不要署自己的名字，用“Zhang Wei”代替。不要写地址。（10分）',
    '亲爱的先生/女士：
我写这封信，是想投诉贵店前几天售出的那部电子词典，因为它存在多处质量问题。
首先，它经常无故死机，给我的使用带来了很大不便。其次，屏幕表面已有明显划痕。最后，键盘上还有几个按键无法正常使用。基于这些情况，我强烈要求贵方尽快为我更换一台新的词典，或者全额退款。
如果你们能认真处理并尽快答复，我将不胜感激。
你真诚的，
Zhang Wei',
    '英二,小作文,letter,历年真题,范文',
    '有关文档/英语考研作文/英二/2012.md',
    1,
    20121
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2012_big',
    'english_two',
    2012,
    'big',
    'chart',
    '2012年英二大作文',
    15,
    NULL,
    NULL,
    'Directions:
Write an essay based on the following table. In your writing, you should
describe the table, and
give your comments.
You should write at least 150 words.
Write your essay on the ANSWER SHEET. (15 points)
某公司员工工作满意度调查

满意度：
满意
不清楚
不满意
岁
16. 7%
50. 0%
33. 3%
41〜50岁
0.0%
36. 0%
64. 0%
>50岁

40. 0%
50. 0%
10. 0%',
    'From the table, one can see that employees in different age brackets vary greatly on the employment satisfaction. According to the data provided above, it can be noticed that 40% of the workers over 50 are content with their employment situation. In contrast, 64% of the employees between the age of 41 and 50 are not satisfied with their employment, which is the highest among all the three categories.
The reasons for this situation can be sought in the following factors. Above all, it is bound up with the employers, much higher expectation of the middle- aged people. It is estimated that approximately 54% of the middle-aged workers have to burn the candle at both ends from time to time. Furthermore, it must be pointed out that the people between 41 and 50 are not energetic enough to attain their ambition. Last but not the least, the fact cannot be ignored that most of the staff in their forties cannot see the slightest prospect of promotion.
In view of the arguments above, we can conclude that the current phenomenon is of no surprise. And therefore it can be predicted that it will not vanish in the short run.',
    '请根据所给表格写一篇短文。写作时你应当：1）描述表格；2）发表你的评论。请至少写150词，并写在答题卡上。（15分）',
    '从表格中可以看出，不同年龄段员工的工作满意度存在较大差异。数据显示，50岁以上员工中有40%表示满意，而41至50岁员工中有64%表示不满意，这一比例在各组中最高。
这种现象可以从多个方面解释。首先，中年员工通常承受着更重的家庭与职业双重压力，因此他们对收入、晋升和工作环境的期待也更高。其次，年长员工往往更加稳定，对当前工作状态更容易接受。相比之下，年轻员工虽然不一定特别满意，但仍处于观察和适应阶段，因此选择“不清楚”的比例也较高。
总体而言，企业如果想提升员工满意度，应更加关注不同年龄群体的真实需求，采取更有针对性的管理措施。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2012.md',
    1,
    20122
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2013_small',
    'english_two',
    2013,
    'small',
    'practical_writing',
    '2013年英二小作文',
    10,
    100,
    100,
    '47. Directions:
Suppose your class is to hold a charity sale for kids in need of help. Write
your classmates an e-mail to
1) inform them about the details，and
2) encourage them to participate.
You should write about 100 words on the ANSWER SHEET.
Do not use your own name. Use “ Li Ming” instead.
Do not write the address. (10 points)',
    'Dear all,
I’m writing the mail to have everyone informed that our class intends to hold
a charity sale for those children who are in great need of help.
The charity sale，scheduled at 2:30 pm on Saturday (Mar. 16), will be held at
the Building No. 2 behind the Main Hall. You’re so welcome to attend the event
and bring your donations such as study articles and daily necessities for the
charity sale. All the money raised from the activity and those items that are
not sold out on the sale event will be donated to Beiguan Primary School,
Darning county, Hebei Province to help the students who need help.
Thanks for your attention to the mail. Remember your donation could help
improve a kid’s quality of life.',
    '假设你的班级准备为留学生举办一场毕业晚会。请给全班同学写一封邮件：1）告知晚会信息；2）征求大家对活动的想法。请在答题卡上写约100词。结尾不要署自己的名字，用“Li Ming”代替。不要写地址。（10分）',
    '亲爱的同学们：
我写这封邮件，是想告诉大家，我们班计划为即将毕业的留学生举办一场告别晚会，并希望听取大家的建议。
晚会拟定在本周六晚七点于学校学生活动中心举行。我们目前考虑安排节目表演、合影留念、互动游戏以及简短的告别发言等环节。如果大家对流程、节目安排或现场布置有更好的想法，请尽快告诉我，以便我们进一步完善方案。
期待大家积极参与，共同把这场晚会办得温暖而难忘。
你真诚的，
Li Ming',
    '英二,小作文,practical_writing,历年真题,范文',
    '有关文档/英语考研作文/英二/2013.md',
    1,
    20131
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2013_big',
    'english_two',
    2013,
    'big',
    'chart',
    '2013年英二大作文',
    15,
    150,
    150,
    '48. Directions:
Write an essay based on the following chart. In your writing, you should
1) interpret the chart, and
2) give your comments.
You should write about 150 words on the ANSWER SHEET. (15 points)',
    'The bar chart above demonstrates that a change has taken place in students’
participation in part-time jobs at a certain college. The percentage of students
who take part-time jobs is only slightly different in the groups of freshmen,
sophomores, and juniors, while in students’ final year of college, there appears
a sharp rise.
Among freshmen, the participation percentage of part-time jobs is 67. 77% .
For sophomores, there is an insignificant rise, which is 71. 13% and the
percentage of juniors is 71. 93%，a slight rise as well. But the percentage
indicates a significant change in senior students since it dramatically grows to
88. 24% .
Some possible factors may contribute to this phenomenon. The obvious factor
is that students of grade four have much more time for taking a part-time job
when they have required assignments finished. But another important factor that
cannot be ignored is that as the graduation date is around the corner, students
realize that they need to gain more working experience, which undoubtedly would
improve their competitiveness and may facilitate them to obtain a better job.
Taking a look from a different angle, we may also conclude that the job market
is becoming more and more intensified. Only those who are ready for the
challenges could find a way to accomplish their goals.',
    '请根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请写约150词，并写在答题卡上。（15分）',
    '上面的柱状图显示，某高校学生参加兼职工作的比例在近几年持续上升，从2003年的约67.77%提高到2012年的84%以上。
这一变化反映出大学生越来越重视社会实践。首先，兼职能够帮助学生减轻经济负担，也能让他们更早接触真实的工作环境。其次，通过兼职，学生可以锻炼沟通、合作与解决问题的能力，为今后的就业做准备。当然，兼职也需要适度，如果处理不好，可能会影响正常学习。
因此，我认为高校应当引导学生树立正确的兼职观念，在保证学业的前提下，合理参与社会实践，这样才能真正实现成长与锻炼。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2013.md',
    1,
    20132
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2014_small',
    'english_two',
    2014,
    'small',
    'email',
    '2014年英二小作文',
    10,
    100,
    100,
    'Suppose you are going to study abroad and share an apartment with John, a local student. Write him an email to 1) tell him about your living habits, and 2) ask for advice about living there. You should write about 100 words on the ANSWER SHEET. Do not use your own name. Use "Li Ming" instead. Do not write your address. (10 points)',
    'Dear John,
I''m glad to hear from you. How have you been these days? The purpose of this email is to tell you about my living habits.
Firstly, I never drink or smoke. Neither do I stay up late. Instead, I keep a balanced diet and go to bed before 11 o''clock at night, because I believe burning the midnight oil is harmful to health. Secondly, I''d like to keep my things clean. It is obvious that living in a messy environment results in a chaotic life.
Finally, could you please offer me some proposals as regards living in your city? I''m sure that we can get along well with each other, and our university life would be one of the best times in life.
Yours sincerely,
Li Ming',
    '假设你将出国学习，并与当地学生 John 合租一套公寓。请给他写一封邮件：1）介绍你的生活习惯；2）请他就当地生活给你一些建议。请在答题卡上写约100词。结尾不要署自己的名字，用“Li Ming”代替。不要写地址。（10分）',
    '亲爱的 John：
很高兴收到你的来信。这封邮件主要是想向你介绍一下我的一些生活习惯。
首先，我从不抽烟，也很少熬夜。我习惯保持均衡饮食，晚上十一点前睡觉，因为我认为长期熬夜对身体不好。其次，我比较注意整洁，喜欢把房间和个人物品收拾得井井有条，因为凌乱的环境往往会带来混乱的生活。
最后，也想请你给我一些关于在你所在城市生活的建议。我相信我们会相处愉快，大学生活也一定会成为一段难忘的经历。
你真诚的，
Li Ming',
    '英二,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英二/2014.md',
    1,
    20141
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2014_big',
    'english_two',
    2014,
    'big',
    'chart',
    '2014年英二大作文',
    15,
    150,
    150,
    'Write an essay based on the following chart. In your writing, you should 1) interpret the chart, and 2) give your comments. You should write about 150 words on the ANSWER SHEET. (15 points)',
    'The column chart above clearly reflects the changes in the statistics between urban and rural population in China during the past two decades. For urban dwellers, there was a noticeable jump from 300 million to around 660 million between 1990 and 2010. By contrast, a remarkable decline occurred in the number of rural population during the same period.
At least three primary contributors account for such changes. First and foremost, there are many more jobs and development opportunities in cities. In addition, big cities offer more convenience and stimulation in education, medical care and public services. More importantly, people prefer cities because of the comparatively advanced transportation system.
Generally speaking, the trend of urbanization will continue for quite some time, and coordinated regional development deserves sustained attention.',
    '请根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请写约150词，并写在答题卡上。（15分）',
    '上面的柱状图清楚地反映出1990年至2010年间中国城乡人口数量的变化。城市人口由3亿左右增长到6.6亿左右，而农村人口则呈现下降趋势。
这一变化主要源于城市化进程加快。首先，城市能够提供更多工作机会和更高收入，因此吸引了大量农村劳动力。其次，城市在教育、医疗、交通和公共服务等方面更具优势，使得越来越多的人愿意在城市长期定居。此外，现代化生活方式也增强了城市对年轻群体的吸引力。
总体来看，这一趋势在未来一段时间内仍会延续。但在推进城市化的同时，也应重视区域协调发展与农村建设，避免城乡差距进一步扩大。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2014.md',
    1,
    20142
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2015_small',
    'english_two',
    2015,
    'small',
    'notice',
    '2015年英二小作文',
    10,
    100,
    100,
    '47.Direerions:
Suppose your university is going to host a summer camp for high school students. Write a notice to
1)briefly introduce the camp activities, and
2)call for volunteers.
You should write about 100 words on the ANSWER SHEET.
Do not use your name or the name of your university.
Do not write your address.(10 points)',
    'Notice
A summer camp for high school students will be held on our campus this July, and volunteers are now needed.
The camp will run from July 15 to July 22. It will include lectures, group discussions, talent shows and outdoor activities, all designed to broaden the students'' horizons and enrich their experience. Since several international students will also take part, volunteers with good communication skills and a strong sense of responsibility are especially welcome.
If you are interested, please sign up at the Students'' Union office before July 10.
Students'' Union',
    '假设你的大学将为高中生举办一个夏令营。请写一则通知：1）简要介绍夏令营活动；2）招募志愿者。请在答题卡上写约100词。不要写你的名字，也不要写学校名称和地址。（10分）',
    '假设你的大学将为高中生举办一个夏令营。请写一则通知：1）简要介绍夏令营活动；2）招募志愿者。请在答题卡上写约100词。不要写你的名字，也不要写学校名称和地址。（10分）

通知
学生会将于暑期在校园内举办高中生夏令营，旨在开阔学生视野、丰富生活体验。由于届时还会有部分外国学生参加，现面向全校招募志愿者。
夏令营计划于7月15日开始，7月22日结束，主题为“热爱祖国，热爱世界”。活动内容包括知识竞赛、爱国歌曲比赛、短剧表演等。我们希望报名者热情开朗，善于与他人沟通，尤其能够较好地与外国学生交流。
如有意参加，请尽快与我们联系。
学生会',
    '英二,小作文,notice,历年真题,范文',
    '有关文档/英语考研作文/英二/2015.md',
    1,
    20151
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2015_big',
    'english_two',
    2015,
    'big',
    'chart',
    '2015年英二大作文',
    20,
    150,
    150,
    '48.Directions:
Write an essay based on the following chart. In your writing, your should
1)interpret the chart, and
2)give your comment.
You should write about 150 words on the ANSWER SHEET.',
    'The pie chart provides some interesting data regarding the family expense of urban residents during Spring Festival in our country. As is shown above, the family expense during the holiday is mainly used for gifts, accounting 40 percent of the whole expense.
Obviously, such statistics regarding family expenses reflects the lifestyles of the average urban family in China. To begin with, the most expense used for buying presents suggests that Chinese people are more likely to enjoy a lifestyle of communication during holidays, which may add more flavor to their routine life. What''s more, it is interesting to note that urban residents attach importance to giving others gifts, thereby devoting much of the income to gifts for friends, relatives and family members. In addition, there is no denying in saying that the major factor responsible for the phenomenon is that urban people posses more money available for gifts thanks to the growth of urban economy.
Taking above-mentioned analysisinto account, we can naturally arrive at the conclusion: as the society further develops, the trend mirrored by the table is bound to continue for a couple of years in the forthcoming future.',
    '请根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请在答题卡上写约150词。（15分）',
    '该饼状图展示了我国城市居民春节期间家庭支出的主要去向。其中，礼品支出所占比例最高，达到40%，其后还有聚餐、交通、娱乐等不同项目。
这些数据反映出春节在中国家庭生活中的重要地位。首先，春节是团聚与走亲访友的传统节日，人们在礼品和聚会上的投入自然较多。其次，随着收入水平提高，居民更加愿意在节日期间改善生活质量，增加消费支出，以获得更强的节日体验。
在我看来，这样的消费结构总体是正常的，它既体现了传统文化的延续，也反映了居民消费能力的提升。当然，合理消费、避免攀比仍然值得提倡。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2015.md',
    1,
    20152
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2016_small',
    'english_two',
    2016,
    'small',
    'email',
    '2016年英二小作文',
    10,
    NULL,
    NULL,
    'Suppose you won a translation contest and your friend Jack wrote an email to congratulate you, and ask advice on translation. Write him a reply to
1)thank him;
2)give your advice.
You should write neatly on the ANWSER SHEET.
Do not sign you own name at the end of the letter, use “Li Ming ”instead.
Do not write the address .',
    'Dear Jack,
Thank you for your congratulation! Since you asked for advice on how to translate, I would like to give some suggestions as follows.
To begin with, you should have a right attitude toward translation, because some students think that they learn it just for examination. In addition, you can read some academic works on translation. Third, as a famous saying goes “practice makes perfect”, you should keep practicing every day.
I’m sure you’ll be an excellent translator. Looking forward to your reply!
Sincerely yours,
Li Ming',
    '假设你在一次翻译比赛中获奖，你的朋友 Jack 发邮件向你表示祝贺，并向你请教翻译学习建议。请给他写一封回信：1）感谢他；2）给出你的建议。请工整地写在答题卡上。结尾不要署自己的名字，用“Li Ming”代替。不要写地址。（10分）',
    '亲爱的 Jack：
谢谢你的祝贺。既然你向我请教如何提高翻译能力，我想给你几点建议。
首先，你要对翻译保持正确的态度，不要只把它当成应付考试的工具。其次，你可以多阅读一些与翻译理论和实践相关的书籍，从中学习方法。最后，正如一句名言所说：“熟能生巧。”坚持每天练习，才能真正提高翻译水平。
我相信你一定会成为一名出色的翻译者。期待你的回信。
你真诚的，
Li Ming',
    '英二,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英二/2016.md',
    1,
    20161
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2016_big',
    'english_two',
    2016,
    'big',
    'chart',
    '2016年英二大作文',
    20,
    150,
    150,
    'Write an essay based on the following chart. you should
1) interpret the chart and
2) give your comments.
You should write about 150 words.',
    'The pie chart shows the major purposes of college students'' travel in a certain university. Enjoying scenery ranks first at 37%, followed by relieving pressure at 33%. Making friends, becoming more independent and other purposes account for 9%, 6% and 15% respectively.
The figures suggest that travel has become more than simple entertainment for university students. To begin with, with living standards rising, more students can afford short trips during vacations. In addition, travel provides them with opportunities to broaden their horizons, meet different people and gain practical experience outside the classroom. It is also a useful way to relax from academic pressure.
In my view, this trend is understandable and positive. If students arrange their time wisely, travelling can help them grow both mentally and socially.',
    '请根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请写约150词。（15分）',
    '如图所示，某高校学生外出旅游的目的存在明显差异。数据显示，37%的学生主要是为了欣赏风景，33%的学生是为了缓解压力，另外还有部分学生是为了交朋友、锻炼独立能力或其他原因。
这一现象可以从几个方面理解。首先，随着经济发展和生活水平提高，越来越多大学生具备了承担旅行费用的能力。其次，学生对旅行的认识已不再局限于娱乐，而是把它看作增长见识、开阔眼界和丰富人生经验的重要方式。再次，大学学习节奏较快，旅行也成为释放压力、调整状态的一种有效方式。
总的来说，旅行对大学生成长具有积极意义。只要合理安排时间和预算，它既能放松身心，也能帮助学生提升综合素质。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2016.md',
    1,
    20162
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2017_small',
    'english_two',
    2017,
    'small',
    'letter',
    '2017年英二小作文',
    10,
    NULL,
    NULL,
    'Suppose you are invited by Professor Williams to give a presentation about Chinese culture to a group of international students. Write a reply to
1)Accept the invitation, and
2)Introduce the key points of your presentation.
You should write neatly on the ANWSER SHEET.
Do not sign you own name at the end of the letter, use “Li Ming ”instead.
Do not write the address .',
    'Dear Professor Williams,
I feel really honored to be invited by you to give a presentation to foreign students, and I think I can accomplish this task very well.
In order to introduce Chinese culture to international students clearly, I intend to focus on two points in my presentation. On the one hand, I will attach my importance to Chinese history, for China is an ancient country with a long history of five thousand years. On the other hand, I would like to share Chinese customs, such as pay a New Year Call, eating dumplings on Lantern Festival, and so on.
Thank you again for your invitation. Looking forward to hearing from you soon.
Sincerely yours,
Li Ming',
    '假设 Williams 教授邀请你为一群国际学生做一次关于中国文化的报告。请给他写一封回信：1）接受邀请；2）介绍你报告的主要内容。请工整地写在答题卡上。结尾不要署自己的名字，用“Li Ming”代替。不要写地址。（10分）',
    '亲爱的 Williams 教授：
非常荣幸收到您的邀请，让我为国际学生做一次中国文化主题的报告。我很乐意接受这项任务，并会认真准备。
为了让国际学生更好地理解中国文化，我准备重点介绍两个方面。第一是中国悠久的历史，因为中国有着五千年的文明传统。第二是富有代表性的中国习俗，例如春节拜年、元宵节吃饺子等，这些内容更容易让外国学生产生直观感受。
再次感谢您的邀请，期待尽快收到您的回复。
你真诚的，
Li Ming',
    '英二,小作文,letter,历年真题,范文',
    '有关文档/英语考研作文/英二/2017.md',
    1,
    20171
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2017_big',
    'english_two',
    2017,
    'big',
    'chart',
    '2017年英二大作文',
    20,
    150,
    150,
    'Write your essay on ANSWER SHEET.
You should
1) interpret the chart, and
2) give your comments.
You should write about 150 words .',
    'The chart indicates that both the number of museums in China and the number of museum visitors increased steadily from 2013 to 2015. Museum numbers rose from about 4,165 to 4,679, while visitor numbers climbed from 637 million to 781 million.
Several reasons can explain this trend. First, museums are important places for the public to learn about history, art and culture, so they naturally attract students, families and tourists. Second, with higher living standards, people are more willing to spend time on educational and cultural activities instead of entertainment alone. In addition, local governments have invested more in cultural facilities, which also helps expand public access.
This is a healthy and encouraging development. A society that values museums is usually one that values knowledge, memory and cultural confidence.',
    '请根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请写约150词。（15分）',
    '根据图表数据可以看出，中国博物馆的数量和参观人数都在持续增长。2013年博物馆数量约为416.5万家，到了2015年增加到467.9万家左右；同期参观人数也由6.37亿增长到7.81亿。
之所以越来越多人愿意走进博物馆，主要有两个原因。首先，博物馆是了解历史文化的重要场所，能够帮助公众增长知识、提高文化素养。其次，随着居民生活水平提升，越来越多家庭愿意把参观博物馆作为休闲和教育兼顾的出行方式。
在我看来，这一趋势十分积极。它不仅说明公共文化服务在改善，也说明人们越来越重视精神生活。未来应继续完善博物馆建设，让更多人从中受益。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2017.md',
    1,
    20172
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2018_small',
    'english_two',
    2018,
    'small',
    'email',
    '2018年英二小作文',
    10,
    NULL,
    NULL,
    'Suppose you have to cancel your travel plan and will not be able to visit Professor Smith. Write him an email to
1) Apologize and explain the situation, and
2) Suggest a future meeting
You should write neatly on the ANWSER SHEET.
Do not sign you own name at the end of the letter, use “Li Ming ”instead.
Do not write the address.',
    'Dear Professor Smith,
I was looking forward to my travelling to Oxford, and visiting you in University of Oxford. I am sorry to tell you that I have to cancel my travel plan to your city, thus I am afraid that I couldn’t visit you according to the due course. So I am writing to extend my sincere apology to you.
Actually, I miss you very much, but I was told that my grandfather was ill in hospital. Therefore, I have to visit and accompany him. If possible, I would like to postpone the arrangement and visit you at a later date this year.
I really hope you will accept my sincere apology.
Yours sincerely,
Li Ming',
    '假设你不得不取消旅行计划，因此无法拜访 Smith 教授。请给他写一封邮件：1）道歉并说明情况；2）提出以后再见面的建议。请工整地写在答题卡上。结尾不要署自己的名字，用“Li Ming”代替。不要写地址。（10分）',
    '亲爱的 Smith 教授：
我原本非常期待前往牛津并拜访您，但很遗憾地告诉您，我不得不取消这次旅行，因此无法按原计划见到您。对此我深感抱歉。
事实上，我也非常想与您见面，但我刚得知祖父生病住院，所以必须前去照顾和陪伴他。如果情况允许，我希望把这次会面延期到今年稍晚一些的时候。
真诚希望您能接受我的歉意。
你真诚的，
Li Ming',
    '英二,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英二/2018.md',
    1,
    20181
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2018_big',
    'english_two',
    2018,
    'big',
    'chart',
    '2018年英二大作文',
    20,
    150,
    150,
    'Write an essay based on the chart below. In your writing, you should
1) interpret the chart, and
2) give your comments.
You should write about 150 words.',
    'The pie chart explicitly illustrates the various factors that consumers in a city took into account when choosing restaurants in 2017, consisting of 5 parts, which are features, service, environment, price and other factors. Among them, the proportion of service, environment, price and other factors is 26.8%, 23.8%, 8.4% and 4.7% respectively. By contrast, feature is the top one, accounting for 36.3%.
What has triggered this phenomenon? In the first place, with the advanced development of national economy and personal wealth, people in China have been richer and richer. Therefore, some of them are more likely to focus on the feature of a restaurant other than on the price. In the second place, in order to develop interpersonal relationships and enjoy a comfortable life, more and more people pay attention to service and environment of a restaurant, instead of concentrating on the price.
Taking into account what has been mentioned above, I think this phenomenon is normal and will continue in the future.',
    '请根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请写约150词。（15分）',
    '该饼状图展示了某城市消费者在2017年选择餐馆时考虑的主要因素。特色、服务、环境、价格及其他因素分别占据不同的比重，其中“特色”所占比例最高，为36.3%。
这一现象背后有多方面原因。首先，随着经济发展和个人收入提高，越来越多消费者不再只看价格，而更重视餐馆是否有独特风格和体验。其次，人们在外就餐时往往也注重社交与放松，因此服务和环境的重要性自然随之提升。
总体而言，这样的消费取向反映出居民生活水平和消费观念的变化。未来餐饮行业若想保持竞争力，就需要在菜品特色、服务质量和用餐体验上持续改进。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2018.md',
    1,
    20182
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2019_small',
    'english_two',
    2019,
    'small',
    'email',
    '2019年英二小作文',
    10,
    100,
    100,
    'Suppose Professor Smith asks you to plan a debate on the theme of city traffic. Write him an email to
1) suggest a specific topic with your reasons, and
2) tell him your arrangement.
You should write about 100 words on the ANSWER SHEET.
Do not sign your own name. Use “Li Ming” instead.
Do not write your address.',
    'Dear Professor Smith,
I am delighted to plan the debate on city traffic. I would like to suggest the topic "Private Cars: Convenience or Burden?"
This topic is both practical and debatable. On the one hand, private cars make daily travel easier and more flexible. On the other hand, they contribute to traffic jams, parking shortages and environmental pollution, which are major concerns in modern cities.
As for the arrangement, I propose holding the debate next Friday evening in the Students'' Activity Center. Two teams can discuss the issue from the perspectives of efficiency, public management and green transport.
I look forward to your advice.
Yours sincerely,
Li Ming',
    '假设 Smith 教授让你策划一场以城市交通为主题的辩论赛。请给他写一封邮件：1）建议一个具体辩题并说明理由；2）告知你的安排。请在答题卡上写约100词。结尾不要署自己的名字，用“Li Ming”代替。不要写地址。（10分）',
    '亲爱的 Smith 教授：
很高兴有机会为这场以城市交通为主题的辩论赛做策划。我建议将辩题定为“私家车增加究竟是便利还是负担？”
之所以这样安排，是因为这一问题与现代城市生活联系最紧密，也最容易引发不同观点的碰撞。一方面，私家车提高了出行效率和生活便利性；另一方面，它也带来了拥堵、停车压力和环境污染等问题。辩论赛可安排在学生活动中心举行，并邀请正反双方围绕交通效率、城市管理和绿色出行展开讨论。
期待您的回复。
你真诚的，
Li Ming',
    '英二,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英二/2019.md',
    1,
    20191
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2019_big',
    'english_two',
    2019,
    'big',
    'chart',
    '2019年英二大作文',
    20,
    150,
    150,
    'In this section, you are asked to write an essay based on the following chart. In your writing, you should
1) interpret the chart, and
2) give your comments.
You should write about 150 words.',
    'The graph shows the changes in the choices of graduates of a certain university from 2013 to 2018
Date indicates that percentage of obtaining employment is the biggest, but it has declined sharply from 68.1% to 60.7%, while the portion of furthering education increased from 26.3% to 34%, starting business from 1.3% to 2.6%.
The reasons behind might be as follows. First of all, most students would like to find a job so that they could gain in-hand skills and experience that are significant for their future career, as well as support their own life. As college education is considered more and more important, the competition between graduates is intensifying. Therefore, further education becomes essential if they want to compete for a rewarding job. Secondly, it is very challenging to start a business for those who lack the resources and experience, so the portion was very small. However some of graduates think it is beneficial for improving their quality in every aspect to start a business.
Overall, there is never a right or wrong choice in terms of what to do after their graduation. According to their career goals, financial status and personal preferences, they can make the optional choice.',
    '请根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请写约150词。（15分）',
    '该图表展示了某大学毕业生在2013年至2018年间毕业去向的变化。就业比例始终最高，但由68.1%下降到60.7%；继续深造的比例则由26.3%上升到34%；创业人数虽占比不高，但也有所增长。
这说明毕业生的选择正在逐渐多元化。对于多数学生来说，就业仍然是最现实的选择，因为它能带来收入和工作经验。但随着竞争加剧，越来越多学生意识到进一步深造的重要性，希望借此提升竞争力。至于创业，由于门槛较高、风险较大，因此比例仍然较小。
在我看来，这种多样化趋势是积极的。只要毕业生根据自身兴趣、能力和条件作出理性选择，每一种道路都有可能通向成功。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2019.md',
    1,
    20192
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2020_small',
    'english_two',
    2020,
    'small',
    'email',
    '2020年英二小作文',
    10,
    NULL,
    NULL,
    'Suppose you are planning a tour of a historical site for a group of international students. Write an email to
1) tell them about the site, and
2) give them some tips for the tour
Please write you answer on the ANSWER SHEET.
Do not use your own name, use “Li Ming” instead. (10 points)',
    'Dear friends,
As a member of the Students Union, I am writing this letter to tell you that we are going to visit the Great Wall next week.
As you know, the Great wall, which is one of the World Cultural Heritages, embodies this nation’s profound and diversified peoples’ wisdom with its magnificent architecture. During this trip, it is advisable to wear comfortable shoes and sunglasses. Besides, you had better take some water and food with you.
I hope that you will enjoy the journey. If you have any further question, please do not hesitate to reply.
Yours sincerely,
Li Ming',
    '假设你正为一群国际学生策划一次历史遗址参观活动。请给他们写一封邮件：1）介绍这个景点；2）给出参观建议。请把答案写在答题卡上。不要署自己的名字，用“Li Ming”代替。（10分）',
    '亲爱的朋友们：
作为学生会的一员，我写这封信是想告诉你们，下周我们将一起参观长城。
众所周知，长城是世界文化遗产之一，以其宏伟建筑体现了中华民族深厚而多元的智慧。在这次参观中，建议大家穿舒适的鞋子，带上太阳镜。同时，最好随身携带一些水和食物。
希望你们都能享受这次旅程。如有任何问题，欢迎随时回复。
你真诚的，
Li Ming',
    '英二,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英二/2020.md',
    1,
    20201
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2020_big',
    'english_two',
    2020,
    'big',
    'chart',
    '2020年英二大作文',
    15,
    150,
    150,
    'Write an essay based on the chart below. In your writing, you should
1) interpret the chart, and
2) give your comments.
You should write about 150 words on the ANSWER SHEET. (15 points)',
    'As is shown in the chart above, we can learn some information about college students’ reading intentions with mobile phones in a certain university. Generally speaking, with the help of cell phones, the proportions of college students who spend their most time on study take a lion’s share, accounting for up to 59.5%. Then those who would like to spend more time on goofing, obtaining information and others take up approximately 20%, 17% and 2% respectively.
There are diverse reasons contributing to the above trend, but generally speaking, they may put down to the following two aspects. Initially, with the rapid development of economy in recent years, people’s living standards have been improved remarkably. As a result, an increasing number of parents are likely to buy a smart phone for their children, which may provide possibility for students to apply more advanced equipment into study so that their learning efficiency and reading horizons would be improved. At the same time, along with the technology development and Internet advancement, a growing quantity of college students’ reading habits have been changed because of the convenience of mobile phones during their study.
Based on the discussion above, it can be obviously concluded that college students’ reading habits adhere to youngsters’ characteristics and comply with the trait of the new era. According to the data above, it is well predicted that this trend as reflected by the pie chart will continue for a while in the near future. Nevertheless, there are also some potential risks if college students are too addicted to mobile phones; therefore, it is advisable for youngsters to arrange time reasonably when they use cell phones.',
    '请根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请在答题卡上写约150词。（15分）',
    '从图表中可以看出，某高校学生使用手机阅读时，最主要的用途是学习，占59.5%；其次是消遣、获取信息和其他用途。
造成这一现象的原因是多方面的。首先，随着经济发展和生活水平提高，越来越多学生拥有智能手机，这为移动学习提供了物质条件。其次，互联网和信息技术的发展改变了学生的阅读习惯，使他们能够更便捷地获取学习资料和外部信息。当然，手机使用也存在潜在风险，如果缺乏自律，容易分散注意力。
总体而言，手机阅读顺应了时代发展趋势。关键不在于是否使用手机，而在于能否合理安排时间，让它更多服务于学习与成长。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2020.md',
    1,
    20202
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2021_small',
    'english_two',
    2021,
    'small',
    'email',
    '2021年英二小作文',
    10,
    NULL,
    NULL,
    'Suppose you are organizing an online meeting. Write an email to Jack, an international student, to 1) invite him to participate, and 2) tell him the details. You should write neatly on the ANSWER SHEET. Do not use your own name. Use "Li Ming" instead. Do not write your address. (10 points)',
    'Dear Jack,
How are you doing recently? I hope everything goes well. I am writing to invite you to take part in an online meeting. Here I have some details.
The meeting is scheduled to start at 5:00 next Friday afternoon and it will last for about one hour. During the meeting, we will discuss the proposal of organizing the winter camp this year. Some students from other countries will join in it. As an international student, you should know them better than us.
That''s all. I really hope you could share your ideas. If you have any questions, please contact me.
Yours sincerely,
Li Ming',
    '假设你正在组织一次线上会议。请给国际学生 Jack 写一封邮件：1）邀请他参加；2）告知相关细节。请工整地写在答题卡上。不要署自己的名字，用“Li Ming”代替。不要写地址。（10分）',
    '亲爱的 Jack：
你最近怎么样？希望一切顺利。我写这封邮件，是想邀请你参加一次线上会议，下面是一些具体信息。
会议定于下周五下午5点开始，持续约一个小时。会上我们将讨论今年冬令营的组织方案，另外还会有一些来自其他国家的学生参加。作为一名国际学生，你对他们的情况应该比我们更了解，因此非常希望听到你的想法。
以上就是主要内容。如果你有任何问题，请随时联系我。
你真诚的，
Li Ming',
    '英二,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英二/2021.md',
    1,
    20211
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2021_big',
    'english_two',
    2021,
    'big',
    'chart',
    '2021年英二大作文',
    15,
    150,
    150,
    'Write an essay based on the following chart. In your writing, you should 1) interpret the chart, and 2) give your comments. You should write about 150 words on the ANSWER SHEET. (15 points)',
    'The bar chart above clearly presents the result of a survey about how people in a certain city do physical exercise. As we can see, doing sports alone takes up 54.3%, ranking first. What comes next is exercising with friends. The proportions of staying with family and joining a certain association are 23.9% and 15.8% respectively.
Through the analysis of the figures, we can conclude that people are paying more and more attention to sports through various channels. Behind this phenomenon, there are several factors. Firstly, with the improvement of living standards, people begin to emphasize the quality of life. Another factor is that people can do sports with more time thanks to economic development.
Based on all the above, we should do more sports whenever possible, and more convenient ways of exercising will continue to emerge.',
    '请根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请在答题卡上写约150词。（15分）',
    '该柱状图清楚地展示了某城市居民进行体育锻炼方式的调查结果。独自运动的比例最高，为54.3%；其次是与朋友一起锻炼；和家人一起以及参加某种协会活动的比例分别为23.9%和15.8%。
通过这些数据可以看出，人们越来越重视通过不同方式保持身体健康。首先，随着生活水平提高，居民对生活质量的关注也在增强。其次，经济发展使人们拥有更多可支配时间去从事锻炼活动。无论采取哪种形式，锻炼都已成为许多人放松身心、改善健康的重要方式。
总的来说，只要条件允许，我们都应当坚持运动。未来也一定会出现更多便捷的健身方式供人们选择。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2021.md',
    1,
    20212
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2022_small',
    'english_two',
    2022,
    'small',
    'email',
    '2022年英二小作文',
    10,
    100,
    100,
    'Suppose you are planning a campus food festival. Write an email to the international students in your university .
　　1) introduce the food festival;
　　2) invite them to participate.
　　You should write about 100 words on the ANSWER SHEET 2.
　　Do not sign your own name at the end of the letter.Use"Zhang Wei"instead.
　　Do not write the address. (10 points)',
    'Dear Friends,
　　I am so happy that we, the Students''Union, are planning to hold a campus food festival on December 31,202l and we hereby extend to you our sincere invitation to take part in it.
　　Here are some details about it. First of all, you can enjoy various food from different countries,such as sushi, kimch1,pudding,p1zza and so on. Secondly, some interesting games to win prizes are waiting for you and you will also appreciate several, wonderful dancing shows while tasting delicious food.
　　We hope that you would accept the invitation. In case of any problems, please feel free to contact with us.
　　Yours sincerely,
　　Zhang Wei',
    '假设你正在筹办校园美食节。请给你校的国际学生写一封邮件：1）介绍美食节；2）邀请他们参加。请在答题卡2上写约100词。结尾不要署自己的名字，用“Zhang Wei”代替。不要写地址。（10分）',
    '亲爱的朋友们：
我们学生会计划于2021年12月31日举办校园美食节，我非常高兴借此向你们发出诚挚邀请。
关于活动安排，首先你们可以品尝到来自不同国家的各种美食，例如寿司、泡菜、布丁和披萨等。其次，我们还准备了许多有趣的游戏，大家可以在品尝美食的同时参加抽奖和观看精彩的舞蹈表演。
希望你们能够接受邀请。如有任何问题，请随时与我们联系。
你真诚的，
Zhang Wei',
    '英二,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英二/2022.md',
    1,
    20221
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2022_big',
    'english_two',
    2022,
    'big',
    'chart',
    '2022年英二大作文',
    15,
    NULL,
    NULL,
    'Write an essay based on the following chart.In your writing, you should
　　l) interpet the chart and
　　2) give your comments.
　　You should write at least 150 words.
　　Write your essay on ANSWER SHEET 2. (15 points)',
    'As is illustrated in the chart, significant changes have occurred in China''s total express business volume and rural express business volume from 2018 to 2020. The total number is roughly 51 billion in 2018,64 billion in 2019 and around 83 billion in 2020.Similarly.the number of Chinese rural express packages in 2018 is 12 billion, while the number jumped dramatically to 30 billion in 2020.
　　What accounts for the increase? From my perspective, at least three factors contribute to the changes. To begin with,the availability of computers and smartphones is the foremost contributor and the rapid development of information technology enables nearly everybody to have access to the Internet. More importantly,the change is closely related to the rising income of the Chinese people, especially the residents living in rural areas. Finally, the changes are enhanced and promoted by the government''s bettering policies as well as the more convenient channels in the e-commerce field.
　　With the development of China''s economy and society, the rising trend is bound to continue for a couple of years in the future.As far as I am concerned, it''s a positive trend and should be encouraged.',
    '请根据所给图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请至少写150词，并写在答题卡2上。（15分）',
    '如图所示，2018年至2020年间，我国快递业务总量和农村快递业务量都发生了显著变化。全国快递总量从2018年的约510亿件增长到2020年的约830亿件；与此同时，农村快递业务量也从120亿件大幅增长到300亿件。
这一增长可以从几个方面来解释。首先，电脑和智能手机的普及以及信息技术的发展，使越来越多的人能够接入互联网并进行网购。其次，中国居民收入水平，尤其是农村居民收入水平不断提高，从而增强了消费能力。最后，政府相关政策支持以及电商渠道日益便捷，也进一步推动了快递业务的快速发展。
随着中国经济和社会继续发展，这一上升趋势在未来几年仍将持续。从整体上看，这是一个值得肯定的积极变化。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2022.md',
    1,
    20222
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2023_small',
    'english_two',
    2023,
    'small',
    'email',
    '2023年英二小作文',
    10,
    100,
    100,
    'An art exhibition and a robot show are to be held on Sunday, and your friend David asks you which one he should go to.

Write him an email to

1) make a suggestion, and

2) give your reason(s)

You should write about 100 words on the ANSWER SHEET.

Do not use your own name. Use “Li Ming” instead. (10 points)',
    'Dear David,

How are you doing recently? I am writing this email to provide some advice on the exhibition and show.

The detailed information is as follows. First of all, it is highly suggested that you should go to the robot show since I know you are always interested in the robot and machine. In addition, it is advisable for you to know some information about the ticket price and some limitation in advance. Last but not least, it is extremely important that you should utilize this platform to make more friends who have a lot of common with you.

I hope the above information would be useful. I am looking forward to your favorable reply.

Yours sincerely,

Li Ming',
    '周日将同时举办一场艺术展和一场机器人展，你的朋友 David 问你应该去哪一个。请给他写一封邮件：1）给出建议；2）说明理由。请在答题卡上写约100词。不要署自己的名字，用“Li Ming”代替。（10分）',
    '亲爱的 David：
你最近怎么样？我写这封邮件，是想就艺术展和机器人展给你一点建议。
我建议你去看机器人展，因为我知道你一直对机器人和机械很感兴趣。此外，你最好提前了解一下票价和参观限制等信息。更重要的是，你还可以借此机会结识一些和你兴趣相近的朋友。
希望这些信息对你有帮助，期待你的回复。
你真诚的，
Li Ming',
    '英二,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英二/2023.md',
    1,
    20231
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2023_big',
    'english_two',
    2023,
    'big',
    'picture',
    '2023年英二大作文',
    15,
    150,
    150,
    'In your essay, you should describe the picture briefly, interpret the implied meaning and give your comments.You should write about 150 words on the ANSWER SHEET. (15 points)',
    'The above line chart explicitly presents information about the health literacy level of the citizens in China from 2012 to 2021. As is clearly illustrated in the graph, the share of health literacy ascended slightly from 8.8% to 11.58% from 2012 to 2016. Meanwhile, the proportion increased greatly from 11.58% to 25.4% from 2016 to 2021.

Several factors, from my perspective, can be easily found to account for the phenomenon, among which I would like to name three most significant ones as follows. In the first place, it is universally acknowledged that the relevant health departments has spared more efforts to advocate and encourage the public to pay attention to the physical and mental health. In addition, there is no denying that with the rapid development of social media such as short videos, more and more residents can get more health tips online. Last but not least, the sudden outbreak of COVID-19 in 2019 has been a wake-up call for many people, who began to focus on healthy and nutritious diet and do exercise, which is another significant factor that could not be ignored.

Taking all these factors into consideration, we can safely come to the conclusion that the current trend will continue for a while in the near future.',
    '在你的作文中，你应当简要描述图表，解释其所体现的含义，并发表自己的评论。请在答题卡上写约150词。（15分）',
    '上面的折线图清楚展示了2012年至2021年中国居民健康素养水平的变化。图中可以看到，2012年至2016年间这一比例由8.8%小幅升至11.58%，而2016年至2021年间则进一步快速上升到25.4%。
造成这一现象的原因有很多，其中最重要的有三点。首先，相关卫生部门投入了更多精力宣传健康理念，鼓励公众重视身心健康。其次，随着短视频等社交媒体迅速发展，居民能够更方便地在线获取健康知识。最后，2019年新冠疫情的暴发也给很多人敲响了警钟，使他们开始更加关注饮食和锻炼。
综合来看，这一趋势在未来一段时间内还会延续，说明公众健康意识正持续提升。',
    '英二,大作文,picture,历年真题,范文',
    '有关文档/英语考研作文/英二/2023.md',
    1,
    20232
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2024_small',
    'english_two',
    2024,
    'small',
    'practical_writing',
    '2024年英二小作文',
    10,
    100,
    100,
    'Suppose you and your friend Jack will do a survey on the protection of old houses in an acient town.You should
1)put forward a plan and
2)ask for opinion.

You should write about 100 words on the ANSWER SHEET. Do not use your own name.Use "Li Ming"instead(10 points)',
    'Dear Jack,
I hope this email finds you well. I am writing this letter to share with you my plan for our research on the preservation of old houses.
There are two main aspects that come to mind. Firstly, we can consult relevant ancient books in the local library to understand the historical knowledge of this ancient town and obtain relevant information about the protection of buildings. Secondly, we should visit the local or surrounding residents to have a deep understanding of the current protection status of ancient buildings.
To collaborate on this project, your insights would be invaluable in shaping the survey questionnaire and determining the best approach for data collection. Could we schedule a time to discuss this further?
I look forward to hearing your thoughts.
Best regards,
Li Ming',
    '假设你和朋友 Jack 将对某古镇老房子的保护情况做一项调查。请写信给他：1）提出你的计划；2）征求他的意见。请在答题卡上写约100词。不要署自己的名字，用“Li Ming”代替。（10分）',
    '亲爱的 Jack：
希望你一切都好。我写这封信，是想和你分享一下我们关于古镇老房子保护情况调查的计划。
我想到的主要有两个方面。首先，我们可以去当地图书馆查阅相关古籍，了解这座古镇的历史背景以及建筑保护方面的信息。其次，我们还应走访当地居民或周边住户，深入了解古建筑当前的保护现状。
在设计问卷和确定数据收集方式时，如果能得到你的意见，一定会很有帮助。我们能否找个时间进一步讨论一下？
期待你的想法。
Li Ming',
    '英二,小作文,practical_writing,历年真题,范文',
    '有关文档/英语考研作文/英二/2024.md',
    1,
    20241
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2024_big',
    'english_two',
    2024,
    'big',
    'chart',
    '2024年英二大作文',
    15,
    150,
    150,
    'Write an essay based on the chart below. In your essay, you should
1) interpret the chart, and
2) give your comments.
You should write about 150 words on the ANSWER SHEET. (15 points)',
    'The chart shows the results of a surveyon the benefits of social practice courses for students at a certain college.Based on the chart,91.3%of students say that participating in courses helps them acquire new knowledge.And quite a few students believe that it helps themboost their hands-on skills(84.4%),improve their mood (54.4%),and enhance their ability to collaborate(32.6%).Such information may be interpreted as follows.To begin with,college students’preference for social practice courses can be justified by the fact that through the courses,college students have the opportunity to put what they have learned into practice,which in turn will enrich their experience and deepen their understanding of related knowledge.In addition,due to the lack of practical experience among college students,such courses provide a great platform for them to improve their hands-on abilities,which is something they value.To sum up,it is evident thatsocial practice courses are highly popular among college students,and it is crucial for colleges to actively promote such courses.',
    '请根据图表写一篇短文。写作时你应当：1）解读图表；2）发表你的评论。请在答题卡上写约150词。（15分）',
    '图表展示了某高校学生对社会实践课程主要收获的调查结果。91.3%的学生认为参加这类课程有助于学习新知识，84.8%的学生认为它能提升动手能力，另外还有部分学生认为它有助于改善心情和增强合作能力。
这一结果说明社会实践课程在大学生中很受欢迎。首先，这类课程能够让学生把书本知识转化为实践经验，从而加深理解。其次，大学生普遍缺乏实际操作经验，因此他们尤其重视动手能力的提升。此外，实践活动通常带有协作性质，也能帮助学生改善情绪、增强团队意识。
总之，社会实践课程确实具有多方面价值，高校应继续重视并积极推广此类课程。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2024.md',
    1,
    20242
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2025_small',
    'english_two',
    2025,
    'small',
    'email',
    '2025年英二小作文',
    10,
    100,
    100,
    'Suppose you are planning a short play based on a classic Chinese novel.Write your friend John an email
　　1) introduce the play and
　　2)invite him take part in it
　　You should write about 100 words on the ANSWER SHEETDo not use your own name. Use “Li Ming’, instead.(10 points)',
    'Dear John,

 I hope you''re well. I''m excited to tell you about a future project that I think you''ll be interested in.

 We are in the process of developing a short play inspired by a classic Chinese novel, which promises to be a riveting blend of tradition and innovation.Your acting abilities and love for the arts make you an excellent candidate for one of our major positions. This is an exceptional opportunity to dig into the depths of Chinese literature while demonstrating your talent. Rehearsals will start next week, and we would be delighted if you could join us. Please let me know if you''re interested or need more.

 Yours sincerely,

 Li Ming',
    '假设你正根据一部中国古典小说筹划一部短剧。请给你的朋友 John 写一封邮件：1）介绍这部短剧；2）邀请他参与演出。请在答题卡上写约100词。不要署自己的名字，用“Li Ming”代替。（10分）',
    '亲爱的 John：
希望你一切都好。我想告诉你一个即将开展的项目，我觉得你一定会感兴趣。
我们正在筹划一部以中国古典小说为基础改编的短剧，它将传统元素与创新表达结合在一起，非常吸引人。你出色的表演能力以及对艺术的热爱，使你非常适合担任其中的重要角色。这不仅是一次展示才华的机会，也能让你更深入地了解中国文学。排练将于下周开始，如果你愿意加入，我们会非常高兴。若你感兴趣或需要更多信息，请告诉我。
你真诚的，
Li Ming',
    '英二,小作文,email,历年真题,范文',
    '有关文档/英语考研作文/英二/2025.md',
    1,
    20251
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO kyyy_writing_essay (
    writing_code,
    exam_direction,
    source_year,
    essay_section,
    prompt_category,
    source_title,
    score_value,
    word_limit_min,
    word_limit_max,
    prompt_content,
    sample_content,
    prompt_translation,
    sample_translation,
    knowledge_tags,
    source_path,
    status,
    sort_no
) VALUES (
    'kyyy_english_two_2025_big',
    'english_two',
    2025,
    'big',
    'chart',
    '2025年英二大作文',
    15,
    150,
    150,
    'Write anessay based on the chart below.In your essay,you should
　　1)incrible and interpret the chart,and
　　2)give your comments.
　　write your answer in about 150 words on the ANSWER SHEET.(15 points)',
    'The bar chart presents a survey of the main daily leisure activities of elderly residents in a certain community. Watching TV ranks first at 90.8%, followed by reading at 86.3%. Gardening and general reading account for 34.7% and 31.8% respectively, while playing chess and cards takes the smallest share at 18.4%.
The chart suggests that older people still rely heavily on low-cost and home-based leisure activities. On the one hand, television and books are easy to access, familiar, and less demanding physically. On the other hand, the relatively lower proportions of outdoor or interactive activities imply that community-level facilities and organized programs may still be insufficient.
In my view, the data reflects both stability and limitation. Respecting seniors'' preferences matters, but communities should also create more diverse and inclusive cultural and fitness opportunities for them.',
    '请根据所给图表写一篇短文。写作时你应当：1）描述并解读图表；2）发表你的评论。请在答题卡上写约150词。（15分）',
    '该柱状图展示了某社区老年居民日常休闲活动的调查结果。看电视所占比例最高，为90.8%；看书位居第二，占86.3%；养花和阅读分别占34.7%和31.8%；下象棋或打牌的比例最低，为18.4%。
这些数据表明，老年人的休闲方式仍然以成本较低、在家即可完成的活动为主。一方面，电视和书籍获取方便、熟悉度高，对体力要求也较低；另一方面，户外性和互动性更强的活动比例偏低，这可能说明社区层面的活动设施与组织供给仍有不足。
在我看来，这组数据既反映出老年人休闲偏好的稳定性，也提示我们需要为他们提供更加丰富、多元和友好的文化与健身选择。',
    '英二,大作文,chart,历年真题,范文',
    '有关文档/英语考研作文/英二/2025.md',
    1,
    20252
)
ON DUPLICATE KEY UPDATE
    prompt_category = VALUES(prompt_category),
    source_title = VALUES(source_title),
    score_value = VALUES(score_value),
    word_limit_min = VALUES(word_limit_min),
    word_limit_max = VALUES(word_limit_max),
    prompt_content = VALUES(prompt_content),
    sample_content = VALUES(sample_content),
    prompt_translation = VALUES(prompt_translation),
    sample_translation = VALUES(sample_translation),
    knowledge_tags = VALUES(knowledge_tags),
    source_path = VALUES(source_path),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    updated_at = CURRENT_TIMESTAMP;
