package br.com.vaultkeeper.magazine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

// @Entity = "esta classe vira uma tabela". Cada campo vira uma coluna.
// Integer (e não int) porque ano e mês podem ser null, como no TypeScript.
@Entity
@Table(name = "magazines")
public class Magazine {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String issue;
    private Integer year;
    private Integer month;

    @Column(name = "cover_model")
    private String coverModel;

    @Enumerated(EnumType.STRING)
    private Condition condition;

    private boolean acquired;

    @Column(name = "is_public")
    private boolean isPublic;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    // O JPA exige um construtor vazio.
    protected Magazine() {
    }

    public Magazine(String title) {
        this.title = title;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public String getCoverModel() { return coverModel; }
    public void setCoverModel(String coverModel) { this.coverModel = coverModel; }
    public Condition getCondition() { return condition; }
    public void setCondition(Condition condition) { this.condition = condition; }
    public boolean isAcquired() { return acquired; }
    public void setAcquired(boolean acquired) { this.acquired = acquired; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
}
