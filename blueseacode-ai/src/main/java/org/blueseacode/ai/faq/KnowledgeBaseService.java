package org.blueseacode.ai.faq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.ChatKnowledgeBase;
import org.blueseacode.dao.mapper.ChatKnowledgeBaseMapper;
import org.blueseacode.dao.util.PageUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final ChatKnowledgeBaseMapper kbMapper;

    /** 根据关键词搜索知识库 */
    public List<ChatKnowledgeBase> search(String keyword) {
        return kbMapper.selectList(
                new LambdaQueryWrapper<ChatKnowledgeBase>()
                        .eq(ChatKnowledgeBase::getStatus, 1)
                        .and(w -> w.like(ChatKnowledgeBase::getTitle, keyword)
                                .or().like(ChatKnowledgeBase::getContent, keyword)
                                .or().like(ChatKnowledgeBase::getKeywords, keyword))
                        .last("LIMIT 3"));
    }

    /** 分页查询知识库（管理端使用） */
    public PageResult<ChatKnowledgeBase> list(String keyword, String category, int page, int size) {
        Page<ChatKnowledgeBase> p = kbMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<ChatKnowledgeBase>()
                        .like(keyword != null && !keyword.isEmpty(), ChatKnowledgeBase::getTitle, keyword)
                        .eq(category != null && !category.isEmpty(), ChatKnowledgeBase::getCategory, category)
                        .orderByDesc(ChatKnowledgeBase::getHitCount)
                        .orderByDesc(ChatKnowledgeBase::getCreateTime));
        return PageUtil.of(p);
    }

    /** 新增知识条目 */
    public void create(ChatKnowledgeBase kb) {
        kb.setHitCount(0);
        kbMapper.insert(kb);
    }

    /** 更新知识条目 */
    public void update(ChatKnowledgeBase kb) {
        kbMapper.updateById(kb);
    }

    /** 删除知识条目 */
    public void delete(Long id) {
        kbMapper.deleteById(id);
    }

    /** 命中计数+1 */
    public void incrementHit(Long id) {
        kbMapper.update(null, new LambdaUpdateWrapper<ChatKnowledgeBase>()
                .eq(ChatKnowledgeBase::getId, id)
                .setSql("hit_count = hit_count + 1"));
    }
}
